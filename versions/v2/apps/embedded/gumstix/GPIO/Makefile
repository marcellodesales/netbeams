
#CC=gcc
CC=/home/teresa/arm-2008q1/bin/arm-none-linux-gnueabi-gcc

CFLAGS=-c -g

all: gpio_reg

gpio_reg: gpio_reg.o
	$(CC) gpio_reg.o -o gpio_reg

gpio_reg.o: gpio_reg.c
	$(CC) $(CFLAGS) gpio_reg.c


clean:
	rm -rf *o gpio_reg
