# Techical Test for The Floow

This is Martin McCallion's implementation of The Floow's Platform Engineer Challenge.

The brief was to count the word frequency in an arbitrarily large file, writing the results to a MongoDB database.

## How to Build and Run

Build using Maven. Run:

```
mvn clean install
```

in the project directory. The jar file will be `target/challenge.jar`.

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
| `-top`              | Takes a number. Prints this many of the words with the highest count, in descending order. |
| `-bottom`           | Takes a number. Prints this many of the words with the lowest count, in ascending order.   |
| `-more`             | If provided, some other statistics are printed.                                            |
| `-excludeStopWords` | If provided, the counting process will ignore [a standard set](https://gist.github.com/sebleier/554280) of English "stop words."      |

If you run multiple instances for parallel processing, you should only specify `-source` on one instance. That one -- the "primary instance" -- will read the file and send its lines to a queue. Other instances will read the queue and do the counting. The primary instances will also process lines from the queue when it has finished reading the file.

You can give any of `-top`, `-bottom` and `-more` on a separate run, without `-source`, to get a report on the current contents of the database.

## Approach

### Analysing the words

The main complexity here was preparing the data to make it suitable for analysing as a body of words. Because the master test file was XML, I decided I had to remove everything that wasn't naturally part of a word. You probably don't want to count "<xml>" as a word differently from "xml", for example.

So I removed all non-word, non-numeric characters, with the exception of hyphens and apostrophes. I kept them in, because hyphenated words -- like "non-numeric", above -- should be counted as one, not two. And apostrophes appear in names like "O'Reilly," and elsewhere.

I kept numbers in for two reasons. I think you'd want things like "A4" to be counted as a word (and certainly not to be counted along with "A"). And also when something like "999" appears in a text, it is effectively a word.

### Distributing the Processing

Splitting the input file into lines was the obvious starting point. This has an obvious limitation: what happens if the input file is just one long line? I decided that was sufficiently unlikely that I could ignore the possibility for now.

I considered a couple of possibilities of how to share out the work. Making a RESTful API call to running servers was one possibility, but it seemed like a lot of overhead to do that for every line of text.

A queueing system is the obvious solution, with one process writing to a queue and _n_ reading from it and processing one line at a time. That keeps the workload on each process small, and allows us to scale up by adding more processors.

Obviously the database is a potential bottleneck, with multiple processes updating the same collection, but that would be the case in any solution.

I considered using a dedicate message queue system, such as Rabbit MQ or Apache Kafka, but since I was already learning MongoDB for this project, I decided that using a MongoDB collection as the queue would be satisfactory. It's likely that a dedicate queueing system would be more robust.

## Limitations

### File Size

The solution is reasonably fast on a file of around 80,000 words, even with just one process running. It gets noticeably faster if three or four are used. I didn't do a detailed analysis of the relative speeds.

On the suggested test file, the Wikipedia dump -- 18 GB zipped, 76 GB unzipped -- I left my MacBook Pro (2017 model, quad-core i7 with 16B of RAM) running for twelve hours overnight, with eight instances going. When I interrupted it, it had processed around 12 million words, and there were still 23 million lines on the queue, and the primary process was still reading the file and writing to the queue.

### Word Analysis

As I described above, I pre-process the lines, removing punctuation characters, and so on. With an XML file this has the effect of creating words like "sitenameWikipediasitename", which is obviously not ideal.



## Possible Alternatives

## Potential Improvements
