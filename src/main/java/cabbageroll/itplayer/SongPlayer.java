package cabbageroll.itplayer;

import com.cryptomorin.xseries.XSound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SongPlayer {

    private final Song song;
    private final Map<Integer, XSound> sounds = new HashMap<>();
    private final List<Player> players = new ArrayList<>();

    private final char BEGIN_DATA = '{';
    private final char END_DATA = '}';
    private final char BEGIN_SOUND = '[';
    private final char END_SOUND = ']';
    private final char SEPARATOR = ':';

    public SongPlayer(Song song) {
        this.song = song;
        parseSounds();
    }

    public void addPlayer(Player p) {
        players.add(p);
    }

    public void start() {
        new Thread(() -> {
            int initialTempo = song.getInitialTempo();
            int initialSpeed = song.getInitialSpeed();
            int[] orders = song.getOrders();
            double TPS = 1 / (2.5 / initialTempo * initialSpeed);
            final double expectedTickTime = 1e9 / TPS;
            long timeLast = System.nanoTime();
            long timeNow;
            double delta = 0;
            int ticksPassed = 0;
            int ordersPassed = 0;
            for (Player p : players) {
                p.sendMessage("tempo: " + initialTempo);
                p.sendMessage("speed: " + initialSpeed);
                p.sendMessage("TPS: " + TPS);
            }
            while (ordersPassed < orders.length) {
                timeNow = System.nanoTime();
                delta += (timeNow - timeLast) / expectedTickTime;
                timeLast = timeNow;
                while (delta >= 1) {
                    tick(ticksPassed++, orders[ordersPassed]);
                    if (ticksPassed >= song.getPattern(orders[ordersPassed]).getRows()) {
                        ticksPassed = 0;
                        ordersPassed++;
                        if (orders[ordersPassed] == 254) {
                            ordersPassed++;
                        } else if (orders[ordersPassed] == 255) {
                            break;
                        }
                    }
                    delta--;
                }
            }
        }).start();

    }

    private void parseSounds() {
        String message = song.getMessage();
        String magic = "ITPLAYER_XSOUNDS";
        int pointer = message.indexOf(magic);
        if (pointer > -1) {
            pointer += 16;
            char[] data = message.toCharArray();
            if (data[pointer++] == SEPARATOR && data[pointer++] == BEGIN_DATA) {
                char c = data[pointer++];
                int index = 0;
                boolean parseIndex = false;
                boolean parseSound = false;
                StringBuilder sb = new StringBuilder();
                while (c != END_DATA) {
                    switch (c) {
                        case BEGIN_DATA:
                            break;
                        case BEGIN_SOUND:
                            sb = new StringBuilder();
                            parseIndex = true;
                            index = 0;
                            break;
                        case SEPARATOR:
                            parseIndex = false;
                            parseSound = true;
                            break;
                        case END_SOUND:
                            sounds.put(index, XSound.valueOf(sb.toString()));
                            parseSound = false;
                            break;
                        default:
                            if (parseIndex) {
                                index = index * 10 + Character.getNumericValue(c);
                            } else if (parseSound) {
                                sb.append(c);
                            }
                            break;
                    }
                    c = data[pointer++];
                }
            } else {
                System.out.println("data is not valid");
            }
        } else {
            System.out.println("data not found");
        }
    }

    private void tick(int tick, int n) {
        Song.Pattern pattern = song.getPattern(n);
        for (int i = 0; i < 64; i++) {
            Song.Pattern.Tile tile = pattern.getRealData()[tick][i];
            if (tile != null) {
                XSound s = sounds.get(tile.getInstrument());
                if (s != null) {
                    // C-4 = 48, C-5 = 60, C-6 = 72
                    float pitch = tile.getNote();
                    for (Player p : players) {
                        p.playSound(p.getLocation(), s.parseSound(), 10f, (float) Math.pow(2, (pitch - 60) / 12));
                    }
                }
            }
        }
    }
}
