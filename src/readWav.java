import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;




class huffmanNode{
    int weight;
    int powerValue;

    huffmanNode left;
    huffmanNode right;
}

class huffComparator implements Comparator<huffmanNode>{
    public int compare(huffmanNode o1, huffmanNode o2) {
        return o1.weight - o2.weight;
    }
}

public class readWav implements ActionListener{

    private JFrame frame;
    private JPanel panel;
    private JButton button;
    private JLabel label;
    private JLabel label2;

    public static long[][] input;
    public static long[][] output;

    public static Vector <Integer> huffCode = new Vector<Integer>();
    public static Vector <Integer> huffValue = new Vector<Integer>();

    public static int newSize;
    public static int oldSize;
    public static float ratio;


    myPanel panel1;


    public static long numSample;
    public static float SamplingRate;

    public readWav(){


        frame = new JFrame();

        panel = new JPanel();
//        panel1= new myPanel();
        button = new JButton("open file");
        button.setSize(50,50);
        button.addActionListener( this);

        label = new JLabel("choose a .wav file");
        label2= new JLabel("");


        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        //panel.setPreferredSize(new Dimension(500,500));
        panel.setLayout(new GridLayout(0,1));
        panel.add(button);
        panel.add(label);
        panel.add(label2);

//        frame.add(panel1);
        frame.setLocationRelativeTo(null);
        frame.add(panel,BorderLayout.SOUTH);
        frame.setPreferredSize(new Dimension(750,750));
//        frame.getContentPane().add(button);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("wav info");
        frame.pack();
        frame.setVisible(true);

    }

    public static List<Integer> LZWencode(String text) {
        int dictSize = 256;
        Map<String, Integer> LZWdict = new HashMap<>();
        for (int i = 0; i < dictSize; i++) {
            LZWdict.put(String.valueOf((char) i), i);
        }

        String target = "";
        List<Integer> code = new ArrayList<>();
        for (char character : text.toCharArray()) {
            String following = target + character;
            if (LZWdict.containsKey(following)) {
                target = following;
            } else {
                code.add(LZWdict.get(target));
                LZWdict.put(following, dictSize++);
                target = String.valueOf(character);
            }
        }
        if (!target.isEmpty()) {
            code.add(LZWdict.get(target));
        }
        return code;
    }

    public static String LZWdecode(List<Integer> encodedText) {
        int dictSize = 256;
        Map<Integer, String> LZWdict = new HashMap<>();
        for (int i = 0; i < dictSize; i++) {
            LZWdict.put(i, String.valueOf((char) i));
        }

        String chars = String.valueOf((char) encodedText.remove(0).intValue());
        StringBuilder afterDe = new StringBuilder(chars);
        for (int code : encodedText) {
            String entry = LZWdict.containsKey(code)
                    ? LZWdict.get(code)
                    : chars + chars.charAt(0);
            afterDe.append(entry);
            LZWdict.put(dictSize++, chars + entry.charAt(0));
            chars = entry;
        }
        return afterDe.toString();
    }

    public static void main(String args[]) throws UnsupportedAudioFileException, IOException {

        new readWav();


    }


    public static int search(int value, Vector<Integer> v){
        for (int i = 0; i< v.size(); i++){
            if(v.get(i) == value) {
                return i;
            }

        }
        return -1;
    }





    public static void printCode(huffmanNode root, String s){
        if(root.left == null && root.right == null && root.powerValue != 2000000000){
//            System.out.println(root.powerValue + "'s code is: " + s);
//            int a = Integer.valueOf(s,2);

            huffCode.add(Integer.parseInt(s,2));
            huffValue.add(root.powerValue);

            return;
        }

        printCode(root.left, s+"0");
        printCode(root.right, s+"1");
    }


    public static long[][] parseWav(String filePath) throws UnsupportedAudioFileException, IOException {
        File file = new File(filePath);

        oldSize = (int) Files.size(Paths.get(filePath));

        AudioFormat wavFile;
        AudioInputStream wavData;
        wavData= AudioSystem.getAudioInputStream(file);
        wavFile=wavData.getFormat();
        System.out.println(wavFile);

        int bytesPerSample = wavFile.getSampleSizeInBits()/8;
        int frameSize= wavFile.getFrameSize();
        System.out.println("frame size is : "+frameSize);


        int sampleSize=wavFile.getSampleSizeInBits();
        int channelNum=wavFile.getChannels();

        InputStream wavStream = new FileInputStream(file);
        InputStream testStream = new FileInputStream(file);

        int headSize = findHeaderSize(testStream);
        System.out.println("tested headSize is :"+headSize);


//        int bufferSize2 = 2;
        int bufferSize4 = 4;
        byte[] buffer4 = new byte[bufferSize4];
//        byte[] buffer2 = new byte[bufferSize2];
        byte[] bufferHeader = new byte[headSize];



        int SubChunkSize = (buffer4[0]) | (buffer4[1]) << 8 | (buffer4[2]) << 16 | (buffer4[3]) << 24;
//        System.out.println(SubChunkSize+" is subchunk 2 size");


        long dataSize = file.length()-headSize;
        long sampleNum = 8*dataSize/wavFile.getSampleSizeInBits();

        System.out.println("number of samples is: " + sampleNum);

        numSample=sampleNum;
        SamplingRate=wavFile.getSampleRate();



        System.out.println("sampling rate is: "+wavFile.getSampleRate());
        long[] channel1 = new long[(int) (dataSize/2/bytesPerSample)];
        long[] channel2 = new long[(int) (dataSize/2/bytesPerSample)];
        long[][] channels = new long[2][];


        // LCL coding
        int[] LCL1 = new int [(int) (numSample/2)];
        int[] LCL2 = new int [(int) (numSample/2)];

        wavStream.read(bufferHeader);

        for(int i=0 ; i< (numSample/2); i++ ) {
            wavStream.read(buffer4);
            channel1[i] = buffer4[0] | (buffer4[1] << 8);
            channel2[i] = buffer4[2] | (buffer4[3] << 8);

//            System.out.println("channel 1 data: " + channel1[i] + " channel 2 data: " + channel2[i]);
            if(i<1){
                LCL1[i] = (int) channel1[i];
                LCL2[i] = (int) channel2[i];
            }

            else{
                LCL1[i] = (int) (channel1[i] - channel1[i - 1]);
                LCL2[i] = (int) (channel2[i] - channel2[i - 1]);
//                System.out.println("LCL1 and LCL2 are: " + LCL1[i] + " " + LCL2[i]);
            }

        }


        // channel coupling
        int[] CCmid = new int[(int) (numSample/2)];
        int[] CCside = new int[(int) (numSample/2)];
        int midLower = CCmid[0];
        int midUpper = CCmid[0];
        int sideLower = CCside[0];
        int sideUpper = CCside[0];

        for(int i=0 ; i< (numSample/2); i++ ) {

            CCmid[i] =  (LCL1[i] + LCL2[i]) / 2;
            CCside[i] = (LCL1[i] - LCL2[i]) / 2;

            midLower =  Math.min(midLower, CCmid[i]);
            midUpper =  Math.max(midUpper, CCmid[i]);

            sideLower =  Math.min(sideLower, CCside[i]);
            sideUpper =  Math.max(sideUpper, CCside[i]);

        }

        System.out.println("the range of CCmid is: [ " + midLower + ", " + midUpper + "]");
        System.out.println("the range of CCside is: [ " + sideLower + ", " + sideUpper + "]");


        int[] combine = new int[(int) numSample];
        for(int i =0; i<numSample/2; i++){
            combine[i]=CCmid[i];
            combine[i+(int)(numSample/2)] = CCside[i];
        }


        // lzw coding
        String combinestr = "";
        String tmpstr = "";
        int[] strlengthlist = new int[combine.length];
        for (int i = 0; i < combine.length; i++) {
            tmpstr = Integer.toString(combine[i]);
            combinestr += tmpstr;
            strlengthlist[i] = tmpstr.length();
        }

        int halfSize = (int)numSample/2;

        List<Integer> combineEncoded = LZWencode(combinestr);

        // write to file

        FileOutputStream media = new FileOutputStream("savedCode.txt");

        for(int i  = 0; i< (combineEncoded.size()); i++){
            media.write(ByteBuffer.allocate(4).putInt(combineEncoded.get(i)).array());
        }
        media.close();


        // lzw decoding

        // read from file

        FileInputStream reader = new FileInputStream("savedCode.txt");
        List<Integer> readFrom = new ArrayList<>();

        for (int i = 0; i<(numSample); i++){
            reader.read(buffer4);
            readFrom.add(ByteBuffer.wrap(buffer4).getInt());
        }


        // lzw decode
        String combineDecoded = LZWdecode(readFrom);

        int idx1 = 0;
        int idx2 = 0;

        for (int i = 0; i < halfSize; i++) {
            idx2 += strlengthlist[i];
        }

        int tmpfirst = 0;
        int tmpsecond = 0;
        long[] lzwdecodeMid = new long[halfSize];
        long[] lzwdecodeDiff = new long[halfSize];

        for (int i = 0; i < halfSize; i++) {
            tmpfirst = Integer.parseInt(combineDecoded.substring(idx1, idx1 + strlengthlist[i]));
            tmpsecond = Integer.parseInt(combineDecoded.substring(idx2, idx2 + strlengthlist[i+halfSize]));

            lzwdecodeMid[i] = tmpfirst;
            lzwdecodeDiff[i] = tmpsecond;
            idx1 += strlengthlist[i];
            idx2 += strlengthlist[i+halfSize];
        }



        // entropy part

//        FileOutputStream media = new FileOutputStream("savedCCmid.txt");
//
//        for(int i  = 0; i< (numSample); i++){
//            media.write(ByteBuffer.allocate(4).putInt(combine[i]).array());
//        }
//        media.close();
//
//
//
//
//        doHuffmanCompress("savedCCmid.txt", "afterCompressed.txt");
//        doHuffmanDe("afterCompressed.txt","deCompressed.txt");



        // read from file.

//        FileInputStream reader = new FileInputStream("deCompressed.txt");


//        for (int i = 0; i<(numSample/2); i++){
//            reader.read(buffer4);
//            readFromMid[i] = ByteBuffer.wrap(buffer4).getInt();
//            readFromSide[i] = ByteBuffer.wrap(buffer4).getInt();
//        }
//
//        for (int i = 0; i<(numSample/2); i++){
//            reader.read(buffer4);
//            readFromSide[i] = ByteBuffer.wrap(buffer4).getInt();
//        }

       // separatedecodedarr1 separatedecodedarr2



        long[] decompressedL = new long[(int)(numSample/2) ];
        long[] decompressedR = new long[(int)(numSample/2) ];




        // decode back -- channel coupling
        for (int i = 0; i<(numSample/2); i++){
            decompressedL[i] = lzwdecodeMid[i]+lzwdecodeDiff[i];
            decompressedR[i] = lzwdecodeMid[i]-lzwdecodeDiff[i];
        }

        long[] finalL = new long[(int)(numSample/2) ];
        long[] finalR = new long[(int)(numSample/2) ];


        // decode back by steps: linear prediction.

        for (int i = 0; i<(numSample/2); i++){
            if(i==0){
                finalL[i] =  decompressedL[i];
                finalR[i] =  decompressedR[i] ;
            }
            else {
                finalL[i] = decompressedL[i] + finalL[i-1];
                finalR[i] = decompressedR[i] + finalR[i-1];
            }
        }


        newSize = (int) Files.size(Paths.get("savedCode.txt"));
        ratio =  (float) (oldSize)/(float)(newSize);


        System.out.println("the old file size is: " + oldSize/1024 + "KB.");
        System.out.println("the new file size is: " + newSize/1024 + "KB.");
        System.out.println("the compression ratio is: " + ratio);

        channels[0]=finalL;
        channels[1]=finalR;



        return input=channels;

    }

    public static int findHeaderSize(InputStream IS) throws IOException {

        byte[] buffer4 = new byte[4];
        byte[] buffer12 = new byte[12];
        byte[] dynamicBuffer;
        long data = 1684108385; // data's ASCII Hex to decimal
        long fromHeader;
        int chunkSize;
        IS.read(buffer12);
        IS.read(buffer4);
        int headerSize = 16;
        fromHeader = (buffer4[0] << 24) | (buffer4[1] << 16) | (buffer4[2] << 8) | buffer4[3];

        while (fromHeader != data) {
            IS.read(buffer4);
            headerSize=headerSize+4;
            chunkSize = buffer4[0] | (buffer4[1] << 8) | (buffer4[2] << 16) | (buffer4[3] << 24);
//            System.out.println("this subchunksize is: "+chunkSize);
            dynamicBuffer = new byte[chunkSize];
            IS.read(dynamicBuffer);
            headerSize=headerSize+chunkSize;

            IS.read(buffer4);
            headerSize=headerSize+4;
            fromHeader = (buffer4[0] << 24) | (buffer4[1] << 16) | (buffer4[2] << 8) | buffer4[3];
        }

        headerSize=headerSize+4;
        return headerSize;

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            JFileChooser fileChooser = new JFileChooser();
            int success = fileChooser.showOpenDialog(null);
            File file = null;
            if (success == JFileChooser.APPROVE_OPTION) {
                file = new File(fileChooser.getSelectedFile().getAbsolutePath());
                parseWav(String.valueOf(file));
//                label.setText("total number of samples is: " + numSample);
//                label2.setText("sampling rate is: " + SamplingRate + "Hz");
                label2.setText("the compression ratio is: " + ratio );
                panel1= new myPanel();
                panel1.removeAll();
                panel1.revalidate();
                panel1.repaint();
                frame.add(panel1);

            }else{
                System.out.println("cannot open the file");
            }

        } catch (UnsupportedAudioFileException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
