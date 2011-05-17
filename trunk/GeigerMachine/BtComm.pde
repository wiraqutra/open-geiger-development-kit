/******************************************************************************
 * Includes
 ******************************************************************************/
//#include "WConstants.h"
//#include "SoftwareSerial.h"
#include "BtComm.h"
/******************************************************************************
 * Definitions
 ******************************************************************************/

/******************************************************************************
 * Constructors
 ******************************************************************************/

extern SoftwareSerial debug ;

BtComm::BtComm()
{
    m_packetSerialNo = -1 ;
    m_firstCharTimeOut = 1000 ;
    m_charTimeOut = 100 ;
    pinMode(PushSwPin, INPUT);
}

/******************************************************************************
 * User API
 ******************************************************************************/

/*********************
 * 初期化と終了.
 ********************/
void BtComm::begin(long speed)
{
    Serial.begin(speed);
}
void BtComm::end()
{
    Serial.end();
}
void BtComm::setTimeOut( int first, int cont )
{
    m_firstCharTimeOut = first ;
    m_charTimeOut = cont ;
}
/*********************
 * モード遷移.
 ********************/
bool BtComm::toCmdMode()
{
    flush();
    delay(500);
    print( "$$$" ) ;
    delay(500);
    char* reply = readString();
    println();
    delay(100);
    flush();
    if( strcmp( reply, "CMD" ) == 0 ) {
        return true ;
    }
    return false ;
}
bool BtComm::toDataMode()
{
    println( "---" ) ;
    return true ;
}
void BtComm::flush()
{
    Serial.flush();
}
/*********************
 * 受信.
 ********************/
char* BtComm::readString()
{
    return readString(m_buf,sizeof(m_buf)) ;
}
/*********************
 * 文字列を受信.
 * コントロール文字が入ってきたらその時点で終了.
 * SWが押されたら、*bufferにSWITCH_PUSHED が入る(マルチタスクになってないので見苦しい)
 ********************/
char* BtComm::readString(char* buffer, int bufmax )
{
    int ptr = 0 ;
    int intData ;
    while( true ) {//ctrl文字以外の最初の文字を見つける.
        intData = read(m_firstCharTimeOut ) ;
        if( intData == CHAR_NONE ) {
            return NULL ;
        }
        if( intData == SWITCH_PUSHED ) {
            buffer[0]=SWITCH_PUSHED;
            buffer[1]=0;
            return buffer ;
        }
        if( intData >= ' ' ) {
            break ;
        }
    }
    ptr = 0 ;
    buffer[0]=(char)intData ;
    ptr = 1 ;
    while( true ) {
        intData = read(m_charTimeOut) ;
        if( intData == SWITCH_PUSHED ) {
            buffer[0]=SWITCH_PUSHED;
            buffer[1]=0;
            return buffer ;
        }
        if( intData < ' ' ) {
            buffer[ptr]=0;
            return buffer ;
        }
        buffer[ptr]=(char)intData ;
        if( ptr >= bufmax - 1 ) {
            buffer[ptr]=0;
            return buffer ;
        }
        ptr ++ ;
    }
}
int BtComm::read()
{
    int data ;
    if( Serial.available() ) {
        data = Serial.read() ;
        return data ;
    }
    if( digitalRead( PushSwPin ) == 0 ) {
        return SWITCH_PUSHED ;
    }
    return CHAR_NONE ;
}
int BtComm::read(int timeout)
{
    int data ;
    timeout /= BtComm_TIME_PERIOD ;
    for( int i = timeout ; i != 0 ; i -- ) {
        if( Serial.available() ) {
            data = Serial.read() ;
#if 0
            debug.print('<',BYTE);
            debug.print(data,BYTE);
            debug.print('>',BYTE);
#endif
            return data ;
        }
        if( digitalRead( PushSwPin ) == 0 ) {
            return SWITCH_PUSHED ;
        }
        delay(BtComm_TIME_PERIOD);
    }
    return CHAR_NONE ;
}

/*********************
 * 送信.
 ********************/
void BtComm::print(uint8_t b)
{
    Serial.print(b);
}
void BtComm::print(const char *s)
{
  while (*s)
    print(*s++);
}

void BtComm::print(char c)
{
  print((uint8_t) c);
}

void BtComm::print(int n)
{
  print((long) n);
}

void BtComm::print(unsigned int n)
{
  print((unsigned long) n);
}

void BtComm::print(long n)
{
  if (n < 0) {
    print('-');
    n = -n;
  }
  printNumber(n, 10);
}

void BtComm::print(unsigned long n)
{
  printNumber(n, 10);
}

void BtComm::print(long n, int base)
{
  if (base == 0)
    print((char) n);
  else if (base == 10)
    print(n);
  else
    printNumber(n, base);
}

void BtComm::println(void)
{
    //print('\r');
    print('\n');
}

void BtComm::println(char c)
{
  print(c);
  println();
}

void BtComm::println(const char c[])
{
  print(c);
  println();
}

void BtComm::println(uint8_t b)
{
  print(b);
  println();
}

void BtComm::println(int n)
{
  print(n);
  println();
}

void BtComm::println(long n)
{
  print(n);
  println();  
}

void BtComm::println(unsigned long n)
{
  print(n);
  println();  
}

void BtComm::println(long n, int base)
{
  print(n, base);
  println();
}

// Private Methods /////////////////////////////////////////////////////////////

void BtComm::printNumber(unsigned long n, uint8_t base)
{
  unsigned char buf[8 * sizeof(long)]; // Assumes 8-bit chars. 
  unsigned long i = 0;

  if (n == 0) {
    print('0');
    return;
  } 

  while (n > 0) {
    buf[i++] = n % base;
    n /= base;
  }

  for (; i > 0; i--)
    print((char) (buf[i - 1] < 10 ? '0' + buf[i - 1] : 'A' + buf[i - 1] - 10));
}
