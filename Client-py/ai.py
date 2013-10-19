import time
from math import sqrt
import sys

EPSILON = sys.float_info.epsilon

class Action(object):
    LEFT = 'Left'
    RIGHT = 'Right'
    DOWN = 'Down'
    UP = 'Up'
    DROP_BOMB = 'DropBomb'
    DETONATE = 'Detonate'
    NONE = 'None'

class Tile(object):
    ME = 'Y'
    PLAYER1 = '1'
    PLAYER2 = '2'
    PLAYER3 = '3'
    PLAYER4 = '4'
    PLAYERS = (ME, PLAYER1, PLAYER2, PLAYER3, PLAYER4)


    HARD_WALL = 'H'
    BRICK_WALL = 'W'
    BOMB = 'B'
    IMPASSABLE = (HARD_WALL, BRICK_WALL, BOMB) + PLAYERS

    EXPLOSION = 'E'
    DEATH_ALERT = 'A'
    DEADLY = (EXPLOSION, DEATH_ALERT)

    EXTRA_BOMB = 'b'
    RANGE = 'r'
    DETONATOR = 'd'
    KICK = 'k'
    POWER_UPS = (EXTRA_BOMB, RANGE, DETONATOR, KICK)

    def __init__(self, value):
        self.value = value

    @property
    def player(self):
        """Returns the player on this tile, or None if no player"""
        p = (set(Tile.PLAYERS) & set(self.value))
        if not p:
            return None
        return list(p)[0]

    def has(self, other):
        """Returns True if this tile has any of the types in other.

        >>> Tile(Tile.ME).has(Tile.ME)
        True
        >>> Tile(Tile.ME).has(Tile.EXPLOSION)
        False
        >>> Tile(Tile.ME + Tile.EXPLOSION).has(Tile.ME)
        True
        >>> Tile(Tile.ME).has(Tile.ME + Tile.BOMB)
        True
        """
        try:
            other = other.value
        except AttributeError:
            pass

        other = set(other)
        me = set(self.value)

        return len(other & me) > 0


    def __str__(self):
        return self.value

    def __repr__(self):
        return self.__class__.__name__ + '(' + repr(self.value) + ')'

class Map(object):
    def __init__(self, game_map):
        self._tiles = [[Tile(col) for col in row] for row in game_map]
        self.width = len(self._tiles)
        self.height = len(self._tiles[0])

    @property
    def tiles(self):
        return self._tiles

    def at(self, p):
        """Get the tile at the specified point

        >>> Map([['', 'B'], ['', '']]).at((1, 0))
        Tile('B')
        """
        return self._tiles[p[1]%self.height][p[0]%self.width]

    def has_LOS(self, p1, p2):
        """Returns True if either point has a straight, unobstructed path to the other point.

        >>> m = Map([['', '', ''], ['', '', ''], ['', 'W', '']])
        >>> m.has_LOS((0, 0), (0, 1))
        True
        >>> m.has_LOS((0, 1), (0, 0))
        True
        >>> m.has_LOS((0, 0), (1, 1))
        False
        >>> m.has_LOS((0, 1), (2, 1))
        True
        >>> m.has_LOS((0, 2), (2, 2))
        False
        """
        if p1[0] != p2[0] and p1[1] != p2[1]:
            return False

        d_x = p1[0] - p2[0]
        d_y = p1[1] - p2[1]

        d_x = int(d_x / abs(d_x)) if d_x else 0
        d_y = int(d_y / abs(d_y)) if d_y else 0

        c_x = p2[0]
        c_y = p2[1]
        while (c_x, c_y) != p1:
            c_x += d_x
            c_y += d_y

            if self.at((c_x, c_y)).has(Tile.HARD_WALL + Tile.BRICK_WALL):
                return False
        return True

    def search(self, c_p, max_distance=None, tiles=None):
        """Get all non-empty tiles within max_distance that contain tiles.

        >>> m = Map([['', '', '', '', ''],      \
                        ['', '', '', '', ''],   \
                        ['', '', '', '', ''],   \
                        ['', '', '', 'W', ''],  \
                        ['', '', 'B', 'W', '']])
        >>> sorted(m.search((2, 3), 2))
        [(2, 4), (3, 3), (3, 4)]
        >>> sorted(m.search((2, 3), 2, 'B'))
        [(2, 4)]
        """
        c_x, c_y = c_p
        lowX = max(0, c_x-max_distance);
        lowY = max(0, c_y-max_distance);
        highX = min(self.width, c_x+max_distance)
        highY = min(self.height, c_y+max_distance)

        results = []
        for col in range(lowX, highX):
            for row in range(lowY, highY):
                tile = self.at((col, row))
                if (not tiles and tile.value) or (tiles and tile.has(tiles)):
                    results.append((col, row))
        return results

    @staticmethod
    def distance(p1, p2):
        """Calculate the distance between two points.

        >>> Map.distance((0, 0), (0, 1))
        1.0
        >>> Map.distance((1, 1), (3, 3))
        2.8284271247461903
        """
        (x1, y1), (x2, y2) = p1, p2
        return sqrt((x1-x2)**2 + (y1-y2)**2)

    def me(self):
        """Return the position of the player.

        >>> Map([['', 'Y'], ['', '']]).me()
        (1, 0)
        """
        for y, row in enumerate(self._tiles):
            for x, col in enumerate(row):
                if col.has(Tile.ME):
                    return (x, y)

class Player(object):
    range = 2
    max_bombs = 1
    detonator = False
    kick = False

    def __init__(self, s):
        self.id = s

    def __repr__(self):
        return self.__class__.__name__ + '(' + repr(self.id) + ')'

class Bomb(object):
    owner = None
    age = 0
    created = 0

    def __init__(self, owner, created):
        self.owner = owner
        self.created = created

class DummyMap(Map):
    def __init__(self):
        self.width = 0
        self.height = 0
        self._tiles = []

    def at(self, p):
        return Tile('')

class AI(object):
    ACTIONS = {
        Action.LEFT: (-1, 0),
        Action.RIGHT: (1, 0),
        Action.DOWN: (0, 1),
        Action.UP: (0, -1),
        Action.DROP_BOMB: (0, 0),
        Action.DETONATE: (0, 0),
        Action.NONE: (0, 0),
    }

    previous_map = DummyMap()
    map = DummyMap()
    game_start = None
    bombs = {}
    players = dict((x, Player(x)) for x in Tile.PLAYERS)

    def time_elapsed(self):
        """Returns the number of seconds since the game started"""
        return time.time() - self.game_start

    def update(self, game_map):
        """Updates the internal representation of the game to match the server.

        >>> ai = AI()
        >>> ai.update([['b', 'B1'], ['', '']])
        >>> list(ai.bombs.keys())
        [(1, 0)]
        >>> ai.bombs[(1, 0)].owner
        Player('1')
        >>> ai.update([['1', 'B'], ['', '']])
        >>> ai.players[Tile.PLAYER1].max_bombs
        2
        """

        if not self.game_start:
            self.game_start = time.time()

        self.previous_map = self.map
        self.map = Map(game_map)

        # Age existing bombs
        for bomb in self.bombs.values():
            bomb.age = self.time_elapsed() - bomb.created

        print()
        print()
        # Find the differences between the old map and the new map
        for y, current_row in enumerate(self.map.tiles):
            for x, current in enumerate(current_row):
                old = self.previous_map.at((x, y))

                if current == old:
                    continue

                current_set = set(current.value)
                old_set = set(old.value)
                added = current_set - old_set
                removed = old_set - current_set

                if added or removed:
                    print((x, y))
                if added:
                    print("Added: ", added)
                if removed:
                    print("Removed: ", removed)

                if 'B' in added:
                    self.bombs[(x, y)] = Bomb(self.players[current.player], self.time_elapsed())
                    print('Add B ' + repr((x, y)))
                elif 'B' in removed:
                    if (x, y) in self.bombs:
                        del self.bombs[(x, y)]
                        print('Remove B  ' + repr((x, y)))
                    else:
                        print('Removed B* ' + repr((x, y)))

                if Tile.RANGE in removed:
                    self.players[current.player].range += 1
                elif Tile.EXTRA_BOMB in removed:
                    self.players[current.player].max_bombs += 1
                elif Tile.KICK in removed:
                    self.players[current.player].kick = True
                elif Tile.DETONATOR in removed:
                    self.players[current.player].detonator = True

    def generate_move(self):
        possible = self.possible_actions()

        score = 0.0
        best = (Action.NONE, (0, 0))

        for p in possible:
            c = self.calculate_score(*p)
            if c > score:
                score = c
                best = p

        return best[0]

    def possible_actions(self):
        possible = []
        me = self.map.me()
        for action, position in AI.ACTIONS.items():
            position = (position[0]+me[0], position[1]+me[1])
            if action == Action.DROP_BOMB or not self.map.at(position).has(Tile.IMPASSABLE):
                possible.append((action, position))
        return possible

    def calculate_score(self, action, p):
        x, y = p

        # Score passability
        passability = sum(1 for d_x, d_y in [(-1, 0), (1, 0), (0, -1), (0, 1)] if not self.map.at((x+d_x, y+d_y)).has(Tile.IMPASSABLE))
        passability /= 4.0

        # Score bombs
        bombs = 0
        if self.bombs:
            for location, bomb in self.bombs.items():
                if self.map.distance(location, p) > bomb.owner.range or not self.map.has_LOS(location, p):
                    continue # Skip bombs that are too far away or don't have line of sight
                if bomb.age > bombs:
                    bombs = bomb.age
            bombs /= max(x.age for x in self.bombs.values()) + EPSILON

        # Place bomb score
        drop = 1 if action == Action.DROP_BOMB else 0

        return 0.85*bombs + 0.1*passability + 0.05*drop



my_ai = AI()

def generate_move(client, game_map):
    global my_ai
    my_ai.update(game_map)
    return my_ai.generate_move()
