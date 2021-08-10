package cabbageroll.itplayer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Song {
    private String name;
    private int orderNum;
    private int instrumentNum;
    private int sampleNum;
    private int patternNum;
    private int createdWithTracker;
    private int compatibility;
    private int flags;
    private int special;
    private int globalVolume;
    private int mixVolume;
    private int pointer = 0;
    private int initialSpeed;
    private int initialTempo;
    private int separation;
    private int pitchWheelDepth;
    private int messageLength;
    private int messageOffset;
    private String reserved;
    private int[] orders;
    private int[] instrumentOffsets;
    private int[] sampleOffsets;
    private int[] patternOffsets;
    private String message;
    private Instrument[] instruments;
    private Sample[] samples;
    private Pattern[] patterns;
    public Song(InputStream is) {
        try {
            // IMPM
            skip(is, 4);

            name = readString(is, 26);

            // PHiligt
            skip(is, 2);

            orderNum = readShort(is);
            instrumentNum = readShort(is);
            sampleNum = readShort(is);
            patternNum = readShort(is);
            createdWithTracker = readShort(is);
            compatibility = readShort(is);
            flags = readShort(is);
            special = readShort(is);
            globalVolume = readByte(is);
            mixVolume = readByte(is);
            initialSpeed = readByte(is);
            initialTempo = readByte(is);
            separation = readByte(is);
            pitchWheelDepth = readByte(is);
            messageLength = readShort(is);
            messageOffset = readInt(is);
            reserved = readString(is, 4);
            skip(is, 64);
            skip(is, 64);
            orders = readByteArray(is, orderNum);
            instrumentOffsets = readIntArray(is, instrumentNum);
            sampleOffsets = readIntArray(is, sampleNum);
            patternOffsets = readIntArray(is, patternNum);

            log("pointer=" + pointer);
            log("instrumentOffsets=" + Arrays.toString(instrumentOffsets));
            log("sampleOffsets=" + Arrays.toString(sampleOffsets));
            log("patternOffsets=" + Arrays.toString(patternOffsets));

            skip(is, messageOffset - pointer);
            message = readString(is, messageLength);

            /*
            instruments = new Instrument[instrumentNum];
            for (int i = 0; i < instrumentNum; i++) {
                instruments[i] = new Instrument(is);
            }
             */

            samples = new Sample[sampleNum];
            for (int i = 0; i < sampleNum; i++) {
                if (pointer != sampleOffsets[i]) {
                    skip(is, sampleOffsets[i] - pointer);
                }
                samples[i] = new Sample(is);
                log("Sample " + i + " created, pointer=" + pointer);
            }

            log("samples loaded");

            patterns = new Pattern[patternNum];
            for (int i = 0; i < patternNum; i++) {
                if (pointer != patternOffsets[i]) {
                    skip(is, patternOffsets[i] - pointer);
                }
                patterns[i] = new Pattern(is);
                log("Pattern " + i + " created, pointer=" + pointer);
            }

            log("patterns loaded");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String example() {
        InputStream is = Song.class.getClassLoader().getResourceAsStream("minecraft.it");
        if (is != null) {
            Song song = new Song(is);
            return song.toString();
        }
        return "null";
    }

    public int getInitialSpeed() {
        return initialSpeed;
    }

    public int getInitialTempo() {
        return initialTempo;
    }

    public String getMessage() {
        return message;
    }

    public int[] getOrders() {
        return orders;
    }

    public Pattern getPattern(int n) {
        return patterns[n];
    }

    @Override
    public String toString() {
        return "Song{" +
            "name='" + name + '\'' +
            ", orderNum=" + orderNum +
            ", instrumentNum=" + instrumentNum +
            ", sampleNum=" + sampleNum +
            ", patternNum=" + patternNum +
            ", createdWithTracker=" + createdWithTracker +
            ", compatibility=" + compatibility +
            ", flags=" + flags +
            ", special=" + special +
            ", globalVolume=" + globalVolume +
            ", mixVolume=" + mixVolume +
            ", initialSpeed=" + initialSpeed +
            ", initialTempo=" + initialTempo +
            ", separation=" + separation +
            ", pitchWheelDepth=" + pitchWheelDepth +
            ", messageLength=" + messageLength +
            ", messageOffset=" + messageOffset +
            ", reserved='" + reserved + '\'' +
            ", orders=" + Arrays.toString(orders) +
            ", instrumentOffsets=" + Arrays.toString(instrumentOffsets) +
            ", sampleOffsets=" + Arrays.toString(sampleOffsets) +
            ", patternOffsets=" + Arrays.toString(patternOffsets) +
            ", \nmessage=" + message +
            ", \ninstruments=" + Arrays.toString(instruments) +
            ", samples=" + Arrays.toString(samples) +
            //", \npatterns=" + Arrays.toString(patterns) +
            '}';
    }

    private void log(String s) {
        if (Main.print) {
            System.out.println(s);
        }
    }

    private void log2(String s) {
    }

    private int readByte(InputStream is) throws IOException {
        pointer++;
        return is.read();
    }

    private int[] readByteArray(InputStream is, int n) throws IOException {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = readByte(is);
        }
        return arr;
    }

    private int readInt(InputStream is) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < 4; i++) {
            bb.put((byte) readByte(is));
        }
        return bb.getInt(0);
    }

    private int[] readIntArray(InputStream is, int n) throws IOException {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = readInt(is);
        }
        return arr;
    }

    private int readShort(InputStream is) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < 2; i++) {
            bb.put((byte) readByte(is));
        }
        return bb.getShort(0);
    }

    private String readString(InputStream is, int n) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            char c = (char) readByte(is);
            if (c == 13) {
                sb.append('\n');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private void skip(InputStream is, int n) throws IOException {
        System.out.println("Skipped: " + readString(is, n));
    }

    public class Sample {
        private final int _00h;
        private final int globalVolume;
        private final int flags;
        private final int defaultVolume;
        private final String name;
        private final int convert;
        private final int defaultPan;
        private final int length;
        private final int loopBegin;
        private final int loopEnd;
        private final int speed;
        private final int sustainLoopBegin;
        private final int sustainLoopEnd;
        private final int samplePointer;
        private final int vibratoSpeed;
        private final int vibratoDepth;
        private final int vibratoRate;
        private final int vibratoWaveformType;
        private final String fileName;

        public Sample(InputStream is) throws IOException {
            log("Sample: pointer=" + pointer);
            // IMPS
            skip(is, 4);

            fileName = readString(is, 12);
            _00h = readByte(is);
            globalVolume = readByte(is);
            flags = readByte(is);
            defaultVolume = readByte(is);
            name = readString(is, 26);
            convert = readByte(is);
            defaultPan = readByte(is);
            length = readInt(is);
            loopBegin = readInt(is);
            loopEnd = readInt(is);
            speed = readInt(is);
            sustainLoopBegin = readInt(is);
            sustainLoopEnd = readInt(is);
            samplePointer = readInt(is);
            vibratoSpeed = readByte(is);
            vibratoDepth = readByte(is);
            vibratoRate = readByte(is);
            vibratoWaveformType = readByte(is);
        }

        @Override
        public String toString() {
            return "\n    Sample{" +
                "fileName='" + fileName + '\'' +
                ", _00h=" + _00h +
                ", globalVolume=" + globalVolume +
                ", flags=" + flags +
                ", defaultVolume=" + defaultVolume +
                ", name='" + name + '\'' +
                ", convert=" + convert +
                ", defaultPan=" + defaultPan +
                ", length=" + length +
                ", loopBegin=" + loopBegin +
                ", loopEnd=" + loopEnd +
                ", speed=" + speed +
                ", sustainLoopBegin=" + sustainLoopBegin +
                ", sustainLoopEnd=" + sustainLoopEnd +
                ", samplePointer=" + samplePointer +
                ", vibratoSpeed=" + vibratoSpeed +
                ", vibratoDepth=" + vibratoDepth +
                ", vibratoRate=" + vibratoRate +
                ", vibratoWaveformType=" + vibratoWaveformType +
                '}';
        }
    }

    public class Pattern {
        private final int length;
        private final int rows;
        private final int[] data;
        private final Tile[][] realData;

        public Pattern(InputStream is) throws IOException {
            log("Pattern: pointer=" + pointer);

            length = readShort(is);
            rows = readShort(is);
            //unknown
            final int maxChannels = 64;
            int channels = 0;

            // nothing
            skip(is, 4);

            data = new int[length];
            for (int i = 0; i < length; i++) {
                data[i] = readByte(is);
            }

            log("Pattern: pointer=" + pointer + ", length=" + length + ", rows=" + rows);
            log("Pattern: data=" + Arrays.toString(data));

            // unpacking
            realData = new Tile[rows][maxChannels];
            Tile[] lastTiles = new Tile[maxChannels];
            for (int i = 0; i < maxChannels; i++) {
                lastTiles[i] = new Tile();
            }
            int[] lastMaskVariables = new int[maxChannels];
            for (int i = 0; i < maxChannels; i++) {
                lastMaskVariables[i] = 0;
            }

            int row = 0;
            for (int i = 0; i < length; i++) {
                int channelValue = data[i];

                log2("Pattern, unpacking: data[" + i + "]=" + channelValue);

                if (channelValue == 0) {
                    row++;
                    continue;
                }

                int channel = (channelValue - 1) & 63;
                if (channel >= channels) {
                    channels = channel + 1;
                }

                int maskVariable;

                if ((channelValue & 128) > 0) {
                    maskVariable = data[++i];
                } else {
                    maskVariable = lastMaskVariables[channel];
                }

                log2("Pattern, unpacked: channel=" + channel + ", row=" + row + ", maskvariable=" + maskVariable);

                Tile tile = new Tile();

                if ((maskVariable & 1) > 0) {
                    tile.setNote(data[++i]);
                }
                if ((maskVariable & 2) > 0) {
                    tile.setInstrument(data[++i]);
                }
                if ((maskVariable & 4) > 0) {
                    //vol/pan
                    int x = data[++i];
                }
                if ((maskVariable & 8) > 0) {
                    //command
                    int x = data[++i];
                    //command value
                    int x2 = data[++i];
                }
                if ((maskVariable & 16) > 0) {
                    tile.setNote(lastTiles[channel].getNote());
                }
                if ((maskVariable & 32) > 0) {
                    tile.setInstrument(lastTiles[channel].getInstrument());
                }
                if ((maskVariable & 64) > 0) {
                    //lastvolpan
                }
                if ((maskVariable & 128) > 0) {
                    //lastcommand
                }

                realData[row][channel] = tile;
                lastTiles[channel] = tile;
                lastMaskVariables[channel] = maskVariable;
            }
        }

        public Tile[][] getRealData() {
            return realData;
        }

        public int getRows() {
            return rows;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("\n    Pattern{" +
                "length=" + length +
                ", rows=" + rows +
                ", data=" + Arrays.toString(data) +
                ",\n\n    realData=");
            for (int i = 0; i < rows; i++) {
                sb.append("\nrow=" + i + Arrays.toString(realData[i]));
            }

            return sb.toString() + '}';
        }

        public class Tile {
            private int note;
            private int instrument;
            private int volume;
            private int panning;
            private int command;
            private int commandValue;

            public int getCommand() {
                return command;
            }

            public void setCommand(int command) {
                this.command = command;
            }

            public int getCommandValue() {
                return commandValue;
            }

            public void setCommandValue(int commandValue) {
                this.commandValue = commandValue;
            }

            public int getInstrument() {
                return instrument;
            }

            public void setInstrument(int instrument) {
                this.instrument = instrument;
            }

            public int getNote() {
                return note;
            }

            public void setNote(int note) {
                this.note = note;
            }

            public int getPanning() {
                return panning;
            }

            public void setPanning(int panning) {
                this.panning = panning;
            }

            public int getVolume() {
                return volume;
            }

            public void setVolume(int volume) {
                this.volume = volume;
            }

            @Override
            public String toString() {
                return "Tile{" +
                    "note=" + note +
                    ", instrument=" + instrument +
                    ", volume=" + volume +
                    ", panning=" + panning +
                    ", command=" + command +
                    ", commandValue=" + commandValue +
                    '}';
            }
        }
    }

    public class Instrument {
        private final String fileName;
        private final int _00h;
        private final int newNoteAction;
        private final int duplicateCheckType;
        private final int duplicateCheckAction;
        private final int fadeOut;
        private final int pitchPanSeparation;
        private final int pitchPanCenter;
        private final int globalVolume;
        private final int defaultPan;
        private final int randomVolumeVariation;
        private final int randomPanningVariation;
        private final int trackerVersion;
        private final int numberOfSamples;
        private final String name;
        private int initialFilterCutoff;
        private int initialFilterResonance;
        private int midiChannel;
        private int midiProgram;
        private int midiBank;

        public Instrument(InputStream is) throws IOException {
            // IMPI
            skip(is, 4);

            fileName = readString(is, 12);
            _00h = readByte(is);
            newNoteAction = readByte(is);
            duplicateCheckType = readByte(is);
            duplicateCheckAction = readByte(is);
            fadeOut = readShort(is);
            pitchPanSeparation = readByte(is);
            pitchPanCenter = readByte(is);
            globalVolume = readByte(is);
            defaultPan = readByte(is);
            randomVolumeVariation = readByte(is);
            randomPanningVariation = readByte(is);
            trackerVersion = readByte(is);
            numberOfSamples = readByte(is);

            // nothing
            skip(is, 1);

            name = readString(is, 26);

            // lazy
            skip(is, 6);

        }

        @Override
        public String toString() {
            return "Instrument{" +
                "fileName='" + fileName + '\'' +
                ", _00h=" + _00h +
                ", newNoteAction=" + newNoteAction +
                ", duplicateCheckType=" + duplicateCheckType +
                ", duplicateCheckAction=" + duplicateCheckAction +
                ", fadeOut=" + fadeOut +
                ", pitchPanSeparation=" + pitchPanSeparation +
                ", pitchPanCenter=" + pitchPanCenter +
                ", globalVolume=" + globalVolume +
                ", defaultPan=" + defaultPan +
                ", randomVolumeVariation=" + randomVolumeVariation +
                ", randomPanningVariation=" + randomPanningVariation +
                ", trackerVersion=" + trackerVersion +
                ", numberOfSamples=" + numberOfSamples +
                ", name='" + name + '\'' +
                ", initialFilterCutoff=" + initialFilterCutoff +
                ", initialFilterResonance=" + initialFilterResonance +
                ", midiChannel=" + midiChannel +
                ", midiProgram=" + midiProgram +
                ", midiBank=" + midiBank +
                '}';
        }
    }
}
