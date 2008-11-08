
//------------------------------------------------------------------------
// ---- Include Files ----------------------------------------------------
//------------------------------------------------------------------------

#include <fcntl.h>
#include <errno.h>
#include <unistd.h>
#include <sys/mman.h>
#include <sys/ioctl.h>
#include <strings.h>
#include <stdio.h>
#include <stdlib.h>



//------------------------------------------------------------------------
// ----  Memory Location Definitions -------------------------------------
//------------------------------------------------------------------------

#define MAP_SIZE 4096
#define MAP_MASK (MAP_SIZE - 1)

// Register definition from the PXA27x Processor Family Developer's Manual
#define UART_REGISTER_BASE_OFFSET  0x40100000  // pg.575
#define GPIO_REGISTER_BASE_OFFSET  0x40E00000  // pg.1005
#define PXA270_MCR 0x40100010
#define PXA270_MSR 0x40100018
#define PXA270_GPLR1 0x40E00004  // read-only
#define PXA270_GPSR1 0x40E0001C	 // write-only


//----------------------------------------------------------------
//-------PXA270 Bit Masks-----------------------------------------
//----------------------------------------------------------------
// MCR - Modem Control Register

#define DTR 0x001;
#define RTS 0x002;
#define OUT1 0x004;
#define OUT2 0x008;
#define LOOP 0x010;
#define AFE 0x020;

// GPLR1 - GPIO Pin Level Register 1; pg. 1019
#define PL40 0x100;
#define PL41 0x200;

// GPSR1 - GPIO Pin-Output Set Register 1; pg. 1005
#define PS40 0x100;
#define PS41 0x200;

//----------------------------------------------------------------
// ---- Global Variables -----------------------------------------
//----------------------------------------------------------------

typedef unsigned int u32;
void *map, *regaddr;



//--------------------------------------------------------------------
// ----  Function Prototypes -----------------------------------------
//--------------------------------------------------------------------

static void putmem (u32 addr, u32 val); //Write to a memory location
static u32 getmem (u32 addr);           //Get register address
void init (int fd, int var);            //Initialize the mem file descriptor 
void register_UART_mem_init (int fd);
void register_GPIO_mem_init (int fd);
u32 get_MCR_reg(void);
u32 get_MSR_reg(void);
u32 get_GPLR1_reg(void);
void set_MCR_reg(void);
void set_GPSR1_reg(u32 gplr1);

//--------------------------------------------------------------------
// ----  Main Function -----------------------------------------------
//--------------------------------------------------------------------

int main(int argc, char **argv)
{
	int fd;
	int mcr, msr, gplr1, gplr2;
	
	if ((fd = open("/dev/mem", O_RDWR | O_SYNC)) < 0) {
		perror("open");
		exit(1);
	}


	// Start the main routine
	printf("\n\n");
	printf("   --------------------------------------------------\n");
	printf("   | Display Register Contents.                     |\n");
	printf("   --------------------------------------------------\n\n");

	
	register_UART_mem_init(fd);	
	mcr = get_MCR_reg();
	msr = get_MSR_reg();
	set_MCR_reg();

	register_GPIO_mem_init(fd);
	gplr1 = get_GPLR1_reg();

	printf("[1] MCR register value is: %x\n", mcr);
	printf("[1] MSR register value is: %x\n", msr);
	printf("[1] GPLR1 register value is: %x\n", gplr1);

	set_GPSR1_reg(gplr1);
	gplr2 = get_GPLR1_reg();
	printf("[2] GPLR1 register value is: %x\n", gplr2);

	close(fd);

	printf("\n\nFinished reading register contents.\n\n");

	munmap(0, MAP_SIZE);

	return 0;
}

//--------------------------------------------------------------------
// ----  Functions ---------------------------------------------------
//--------------------------------------------------------------------


//**************************************************************************//
// init - Initialize the memory map
//**************************************************************************//
void init(int fd, int var)
{
	map = mmap(0, MAP_SIZE, PROT_READ | PROT_WRITE, MAP_SHARED, fd,
			   var & ~MAP_MASK);

	if (map == (void*) - 1) {
		perror("mmap()");
		exit(1);
	}
}


//**************************************************************************//
// putmem - Write value to the register located at address.
//**************************************************************************//
static void putmem (u32 addr, u32 val)
{
	regaddr = map + (addr & MAP_MASK);
	*(u32*) regaddr = val;
}


//**************************************************************************//
// getmem - Get the register address
//**************************************************************************//
static u32 getmem(u32 addr)
{
	u32 val;

	regaddr = map + (addr & MAP_MASK);
	val = *(u32*) regaddr;

	return val;
}

void register_UART_mem_init (int fd)
{
	init(fd,UART_REGISTER_BASE_OFFSET);  
}


void register_GPIO_mem_init(int fd)
{
	init(fd, GPIO_REGISTER_BASE_OFFSET);
}


u32 get_MCR_reg()
{
	return getmem(PXA270_MCR) & 0xffff;
}


u32 get_MSR_reg(void)
{
	return getmem(PXA270_MSR) & 0xffff;
}

u32 get_GPLR1_reg(void)
{
	return getmem(PXA270_GPLR1) & 0xffff;
}


void set_MCR_reg()
{
	int mcr = getmem(PXA270_MCR);
	mcr &= 0x00;
	putmem(PXA270_MCR, mcr);
}


void set_GPSR1_reg(u32 gpsr1)
{
	gpsr1 |= PS40;
	gpsr1 |= PS41;
	fprintf(stderr, "new gpsr1 is: %x\n", gpsr1);
	putmem(PXA270_GPSR1, gpsr1);

}
