# Technical Test for The Floow

This is Martin McCallion's implementation of The Floow's Platform Engineer Challenge.

The brief was to count the word frequency in an arbitrarily large file, writing the results to a MongoDB database.

## How to Build and Run

Build using Maven. In the project directory, run:

```
mvn clean install
```

The jar file will be `target/challenge.jar`.

Run as follows:

```
java -jar target/challenge.jar [–source <FILE-NAME>] [-mongo localhost:27017] [-excludeStopWords] [-top 10] -[bottom 10] [-more] 
```

Terms in [square brackets] are optional. The meanings of the parameters are given below.

### Command-Line Parameters

All parameters are optional.

| Parameter         | Meaning                                                                                    |
| ----------------- | ------------------------------------------------------------------------------------------ |
| `-source`         | The path to the file containing the corpus of words                                        |
| `-mongo`          | The MongoDB details, in the form `host:port`. If not provided, `localhost:27017` is used.  |
| `-excludeStopWords` | If provided, the counting process will ignore [a standard set](https://gist.github.com/sebleier/554280) of English "stop words."      |
| `-top`              | Takes a number. Prints this many of the words with the highest count, in descending order. |
| `-bottom`           | Takes a number. Prints this many of the words with the lowest count, in ascending order.   |
| `-more`             | If provided, some other statistics are printed.                                            |

If you run multiple instances for parallel processing, you should only specify `-source` on one instance. That one -- the "primary instance" -- will read the file and send its lines to a queue. Other instances will read the queue and do the counting. The primary instance will also process lines from the queue when it has finished reading the file.

You can give any of `-top`, `-bottom` and `-more` on a separate run, without `-source`, to get a report on the current contents of the database.

## Approach

### Analysing the words

The main complexity here was preparing the data to make it suitable for analysing as a body of words. Because the master test file was XML, I decided to remove everything that wasn't naturally part of a word. You probably don't want to count "<xml>" as a word differently from "xml", for example.

So I removed all non-word, non-numeric characters, with the exception of hyphens and apostrophes. I kept those in, because hyphenated words -- like "non-numeric", above -- should be counted as one, not two. And apostrophes appear in names like "O'Reilly," and elsewhere.

I kept numbers in for two reasons. I think you'd want things like "A4" to be counted as a word (and certainly not to be counted along with "A"). And also when something like "999" appears in a text, it is effectively a word.

### Distributing the Processing

Splitting the input file into lines was the obvious starting point. This has an evident limitation: what happens if the input file is just one long line? I decided that was sufficiently unlikely that I could ignore the possibility for now.

I considered a couple of possibilities of how to share out the work. Making a RESTful API call to running servers was one possibility, but it seemed like a lot of overhead to do that for every line of text.

A queueing system is the obvious solution, with one process writing to a queue and others reading from it, and processing one line at a time. That keeps the workload on each process small, and allows us to scale up by adding more processors.

The database is a potential bottleneck, with multiple processes updating the same collection, but that would be the case in any solution.

I considered using a dedicated message queueing system, such as Rabbit MQ or Apache Kafka, but since I was already learning MongoDB for this project, I decided that using a MongoDB collection as the queue would be satisfactory. It's likely that a dedicated queueing system would be more robust.

## Limitations

### File Size

The solution is reasonably fast on a file of around 80,000 words, even with just one process running. It gets noticeably faster if three or four are used. I didn't do a detailed analysis of the relative speeds (but see below in the "Notes on the Desirable Goals" section).

On the suggested test file, the Wikipedia dump -- 18 GB zipped, 76 GB unzipped -- I left my MacBook Pro (2017 model, quad-core i7 with 16GB of RAM) running for twelve hours overnight, with eight instances going. When I interrupted it, it had processed around 12 million words, and there were still 23 million lines on the queue, and the primary process was still reading the file and writing to the queue.

### Word Analysis

As I described above, I pre-process the lines, removing punctuation characters, and so on. With an XML file this has the effect of creating words like "sitenameWikipediasitename", which is obviously not ideal.

### Case Sensitivity

The word count is case-sensitive. This has a potential downside: for example "however" will be counted differently depending on whether or not it starts a sentence. However, we have to balance that against the desire for references to a baker being kept distinct from references to people with the surname Baker.

It also means that typos like "BAker" will be counted separately again.

The only way I can see around these conflicting requirements would be to have an option to merge the various versions at the point of displaying the count.

The exception to all this is that the stop words are checked case-insensitively.

### Zipped Files

Since the Wikipedia dump comes zipped (actual BZ2 compressed), I investigate the possibility of running the process directly from compressed archives. There is code in place that attempts to do this. In theory it ought to be possible. However, when I ran it against the compressed file, it seemed to be picking up arbitrary groups of bytes as words, so there's something not quite right. I've left the code in place, though.

## Notes on the Desirable Goals

### Failure of a Server

If a process were to fail, the other processes would continue without fuss.

### Report on Statistically Interesting Words

See the `-more` parameter. The total number of unique words, and the number of words that are only used once are printed.

### Tradeoffs Between Efficiency and Accuracy

I ran against a corpus of around 640,000 words (a long ebook converted to plain text). Using a single process, it took 13 minutes. Using five (including the primary one which does the enqueueing first), it took less than a minute.

The results in both cases were the same for the top and bottom ten words, and similarly for the top 100 most frequently-used words. However, there were some small differences in the counts of unique words and the number that were used only once. This leads me to believe that there is some problem caused by having multiple processes.

The most likely issue is contention for processes trying to count the same word. Both processes would have to update the same MongoDB document, so a race condition is possible. This would be very hard to debug.

As to the master test file, since I couldn't complete processing it in a reasonable time on the equipment available, there's not much I can say about that.



