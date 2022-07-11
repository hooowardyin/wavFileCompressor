# wavFileCompressor
a wavFile compressor and decompressor

.WAV file compression
Implement method
a) read the .WAV file
This part is same as Project 1. The big idea is that we parse the .WAV header first to get the basic information of the file, after that, we store the samples information to two arrays as our left channel and right channel.
A normal .wav header usually has a length of 44 or 46 bytes, but it is not always true. The right method to get the header length is to read the size of each chunk and then jump to the next chunk until reach the data chunk. All the data after dataSize will the real sound data. dataSize*8/BitsPerSample will give the total number of samples in the file. ByteRate*8/BitsPerSample will give the sampling rate.
By reading the data NumChannels can get the number of channel in the file, similarly, BitsPerSample giving sample size in bit. With these two data, we can keep analyze the real sound data. if BitsPersample is 16 bits and NumChannels is 2, then very sample has a length of 2 bytes and every left channel goes before every right channel. We create two arrays to store each sample in these two channels. The sound data is placed in little-endian order, so when parsing them into the array we manually change the byte order in to big-endian. For example, in fig.1, the right channel of sample 2 is1ef3, we shift f3 8-bit left then “OR” with 1e. f31e will be the big-endian hex data.

b) Compression
The main contents to compress is the sound samples. For the header, we don’t have much approaches to compress it. Header can be passed to the compressed file directly.
Three steps are used in my program to compress the .WAV file samples:

1. Linear prediction
2. Channel coupling
3. Entropy coding
and a reverse order will be used to decompress back to original.

1) Linear prediction
linear prediction is using the difference of two sample to express the second sample.
In my program, the first sample of the .WAV file is save as the reference value to the first of the array. The following elements are the difference of element [i] and element [i-1].
The reason to choose this method is that sounds are physically sine form wave. They change in term of time. Their difference between a very short time is small. I can record these difference by using less bits to save the memory.

2) Channel coupling
Channel coupling is an audio compression technique in which the redundancy of information between audio channels is reduced.
In my program, I save the average of two channels (l+r)/2 to an array and saving the half difference to another array like (l-r)/2.
We call it mid-coding and side coding. After doing this, we can use less bits to represent the two channels information, because the differences between two channels are not significant big.

3) Entropy coding
entropy coding is lossless coding method. It can reduce the number of bit to represent the information. In this step, LZW coding is used to do the compression (same as what we used in programming assignment 2).
LZW encoding uses fixed-length codewords to represent variable-length strings of
symbols or characters that commonly occur together.
The encoder is building the dictionary dynamically during the encoding process.
The strategy is to place longer and longer repeated entries into a dictionary, after that use code to represent the element other than the element itself.

c) compression ratio
compression ratio is the (size of original .WAV file)/(size of compressed file).
We simply read the size of these two files and then make the division will get the ration. In Java, Files.size() can give the size of a file in bytes.

d) decompression
we parse the header first exactly same as what we did in step a). After we get the information, we do the reverse steps of the compression.
LZW decoding first, followed by channel coupling decoding, and linear prediction decoding. LZW decoding is achieved by taking each code from the compressed file and translating it through the code table to find what character or characters it represents.
For the channel coupling decoding, we add mid value to side value and then we will get the left channel:
(l+r)/2 + (l-r)/2 = l
we do the subtraction, the right channel will be given:
(l+r)/2 - (l-r)/2 = r
    
last is the linear prediction decoding. We simply add element[i] to element[i-1], and then we can get the original sound samples. (for the first element, keep it as reference value.)
