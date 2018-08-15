# Transcoder
Detect UTF-16 files and transcode them to UTF-8. Can be adapted to transcode to any Charset

## What is it?
A sample program that detects if files in an input directory are using UTF-16 (BE or LE), converts it to UTF-8 and output the new file in an output directory and move the input file to an archived directory.

## Command line parameters
--sourceDir=<source directory>
Input directory for files to be processed, defaults to "./in"
--targetDir=<target directory>
output directory for generated files, defaults to "./out"
--archivedDir=<archieved dirctory>
archived directory where processed input files are moved after conversion, defaults to "./archived"

## FAQ
### Why is called a sample?
It was done quickly as a proof of concept for demonstration purpose. There are no unit test and was barely tested. It requires some cleanup and some additionnal error handling.
### How I can use it?
You can complete it to use it for yourself or can look ath interesting piece of codes. Interesting bits are the cide that detects UTF-16 (or any byte headers that could be added) and the code that read it in one charset and write it in another. Simple bits of code but useful.
### Are there other alternative for files conversion?
Plenty. As an example, if you have an ESB, an ETL or an EIP middleware, it is probably already configurable and you have way more options.
### Anything else?
Have fun!
 
