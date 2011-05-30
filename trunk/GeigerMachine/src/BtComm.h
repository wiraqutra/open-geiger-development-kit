#ifndef BtComm_h
#define BtComm_h

#include <inttypes.h>
//#include "WProgram.h"
#define BtComm_RECV_BUF_MAX 48
#define BtComm_TIME_PERIOD 8
#define PushSwPin 12 //Bluetooth操作用SW

typedef char* PTRK ;

class BtComm
{
 private:
    uint8_t m_packetSerialNo ;//パケットシリアル番号.
    char m_buf[BtComm_RECV_BUF_MAX];//送受信バッファ.
    int m_firstCharTimeOut ;//最初の1文字のタイムアウト
    int m_charTimeOut ;//連続して入力される文字のタイムアウト
    static const int CHAR_NONE = -1 ;
 public:
    static const char SWITCH_PUSHED = -2 ;
    BtComm();
    void begin(long);
    void end();
    void flush();

    void setTimeOut( int , int  );
    bool toCmdMode();
    bool toDataMode();

    char* readString();
    char* readString(char* buffer, int max );
    int read();
    int read(int timeout);

    void print(char);
    void print(const char[]);
    void print(uint8_t);
    void print(int);
    void print(unsigned int);
    void print(long);
    void print(unsigned long);
    void print(long, int);
    void println(void);
    void println(char);
    void println(const char[]);
    void println(uint8_t);
    void println(int);
    void println(long);
    void println(unsigned long);
    void println(long, int);
 private:
    void printNumber(unsigned long n, uint8_t base);
};

#endif

