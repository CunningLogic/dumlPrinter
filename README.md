A DJI Duml Packet Parser, prints (most) information stored witin a duml packet in an easy to read format.

dumlPrinter 0.1 - by Jon Sawyer - jon@cunninglogic.com

Usage:
java -jar dumlPrinter.jar <packet>

Example:
java -jar dumlPrinter.jar  550D04332A2835124000002AE4


java -jar ~/bin/dumlPrinter.jar "55 0D 04 33 2A 28 35 12 40 00 00 2A E4"
Packet:		550D04332A2835124000002AE4
CRC32:		58410

Header:		550D0433
Length:		13
Version:	1
CRC8:		51

Transit:	2A283512
Route:		1001 -> 0801
Source:		1
Source ID:	10		PC
Target:		1
Target ID:	8		DM368 Air Side
Sequence:	4661

Command:	400000
cmdType:	64		ACK
cmdSet:		0		Universal
cmdID:		0

