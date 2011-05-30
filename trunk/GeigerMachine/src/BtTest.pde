/*
  動作
    命令は、アルファベット3文字とする。
    命令とパラメーターの間はスペースで区切る。
    命令の最後は、CR/LF/CR+LFのいずれかを使用する。

    命令表
    stt   測定開始(start) 0.5秒間のカウンタ値を出力し続ける
    stp   測定終了(stop)
    ver   ファームウェアのバージョン
    nam   マシン名
    opt   校正データ等の情報
    stt   データ出力形式(タイミング等の)
    nop   何もしない

    現状
    stt
      測定を開始する。0.5秒毎に、0.5秒間のカウント値を、出力する。
    stp
      測定を終了
  プログラム
    bluetoothからの1byte受信(BtComm::read)のところで、新しいデータが
    取得できる場合に取得し、そのデータを送信するようにしている。
    これは、マルチスレッドがないので、しょうがない。
*/

//タイマー割り込み関連
#include <avr/interrupt.h>
#include <avr/io.h>
#define INIT_TIMER_COUNT 6
#define RESET_TIMER2 TCNT2 = INIT_TIMER_COUNT  //マクロ
int IntervalTimerCounter = 0;
bool inMeasure = false ;

//外部IO割り込みカウンタ
int GeigerCounter = 0 ;//ガイガーカウンタ
int LastGeigerCounter = 0 ;//最後のガイガーカウンタ
int MeasureCount = 0 ;//測定回数
int LastMeasureCount = 0 ;//測定


//ソフトウェアシリアル関連
#include <SoftwareSerial.h>
#define rxPin 1
#define txPin 13 //LEDと同じ所を使っている
//#define PushSwPin 12 //Bluetooth操作用 BtComm.h
#define LedPin 11 //LEDと同じ所を使っている

#include "BtComm.h"
#include "TermName.h"
void entryDevice() ;
int strCmp( char* dist, char* src );
void replyPrint( char* str );
void replyPrint( char* title, char* str );

//タイマー割り込み
// Aruino runs at 16 Mhz, so we have 1000 Overflows per second...
// 1/ ((16000000 / 64) / 256) = 1 / 1000
ISR(TIMER2_OVF_vect) {
    RESET_TIMER2;
    IntervalTimerCounter ++ ;
    if (IntervalTimerCounter == 500 ) {//500msec
        IntervalTimerCounter = 0 ;
        LastGeigerCounter = GeigerCounter ;
        GeigerCounter = 0 ;
        MeasureCount ++ ;
    }
};
//外部IO割り込み
void CountInterruptFunc()
{
    GeigerCounter ++ ;
}


// set up a new serial port
SoftwareSerial debug =  SoftwareSerial(rxPin, txPin);
BtComm btComm = BtComm() ;
TermName termName = TermName() ;
void setup() {
    // set the data rate for the SoftwareSerial port
    pinMode(txPin, OUTPUT);
    debug.begin(9600);//38400,115200だとNG.
    btComm.begin(57600);

    pinMode(LedPin, OUTPUT);

    //カウンタの初期化
    IntervalTimerCounter = 0 ;
    MeasureCount = 0 ;
    LastMeasureCount = 0 ;
    inMeasure = false ;
    GeigerCounter = 0 ;
    LastGeigerCounter = 0 ;

    //タイマー割り込み設定
    //Timer2 Settings: Timer Prescaler /64,
    TCCR2B |= (1<<CS22); // turn on CS22 bit
    TCCR2B &= ~((1<<CS21) | (1<<CS20)); // turn off CS21 and CS20 bits
    // Use normal mode
    TCCR2A &= ~((1<<WGM21) | (1<<WGM20)); // turn off WGM21 and WGM20 bits
    // Use internal clock - external clock not used in Arduino
    ASSR |= (0<<AS2);
    TIMSK2 |= (1<<TOIE2) | (0<<OCIE2A); //Timer2 Overflow Interrupt Enable

    RESET_TIMER2;

    sei();//割り込み許可

    // IO割り込み http://www.arduino.cc/en/Reference/AttachInterrupt
    int CounterPin = 2 ;
    int CounterPinNo = 0 ;
    pinMode( CounterPin, INPUT ) ;
    digitalWrite( CounterPin, HIGH ) ;//turn on pullup resistors
    attachInterrupt( CounterPinNo, CountInterruptFunc, FALLING ) ;//LOW, CHANGE, RISING, FALLING


    //openning
    //電源投入時には、電源遮断時の状態が残っていて、コマンドモードになっていることがあるが、
    //強制的に、データ通信モードに移行させる。
    delay(1000);
    btComm.toDataMode();
    debug.println("change jumper sw");
    for( int i = 1 ; i != 0 ; i -- ) {
        digitalWrite(LedPin, i & 1 );
        delay(1000);
        debug.print("start ");
        debug.println(i,DEC);
        debug.println(" ");
    }
    digitalWrite(LedPin,0);
}
/**************************************************
  メインループ
**************************************************/
void loop() {
    while( 1 ) {
        btComm.setTimeOut( 10000, 300 ) ;
        char* buf = btComm.readString() ;
        if( buf == NULL ) {
            continue ;
        }
        if( *buf == (char)btComm.SWITCH_PUSHED ) {
            //ペアリングSWが押された
            inMeasure = false ;
            entryDevice() ;
            debug.println("to data mode\n");
            btComm.toDataMode() ;
            //以下、ENDの空読み
            btComm.setTimeOut( 1000, 100 ) ;
            char* buf = btComm.readString() ;
        }
        else {
            if( strCmp( buf, "stt" ) == 0 ) {
                inMeasure = true ;
            }
            else if( strCmp( buf, "stp" ) == 0 ) {
                inMeasure = false ;
                btComm.println( "ok" ) ;
            }
            else if( strCmp( buf, "nop" ) == 0 ) {
                inMeasure = false ;
                btComm.println( "ok" ) ;
            }
            else if( strCmp( buf, "ver" ) == 0 ) {
                inMeasure = false ;
                btComm.println( "ver 0.00" ) ;
                btComm.println( "ok" ) ;
            }
            else if( strCmp( buf, "nam" ) == 0 ) {
                inMeasure = false ;
                btComm.println( "akiduki 000" ) ;
                btComm.println( "ok" ) ;
            }
            else if( strCmp( buf, "opt" ) == 0 ) {
                inMeasure = false ;
                btComm.println( "ok" ) ;
            }
            else {
                inMeasure = false ;
                btComm.println( "error" ) ;//エラー
            }
        }
    }
}
//デバイス発見とInquiry
void entryDevice() {
    if( btComm.toCmdMode() == false ) {
        debug.println("cmd mode false");
    }
    else {
        debug.println("cmd mode ok");
    }
    //デバイスを見つけ出す
    btComm.setTimeOut( 1000, 10 ) ;
    debug.println("in");
    btComm.println( "in" ) ;
    char* reply = btComm.readString() ;//"Inquiry, COD=0"
    debug.print("Inquiry COD=0:");
    debug.println(reply);

    btComm.setTimeOut( 20000, 100 ) ;
    reply = btComm.readString() ;
    replyPrint(reply);
    if( strCmp( reply, "No" ) == 0 ) {
        debug.println("device zero");
    }
    else if( strCmp( reply, "Found" ) == 0 ) {
        debug.println("reply found");
        replyPrint(reply);
        int NumOfFound = reply[ strlen( reply ) - 1 ] - '0' ;
        debug.print("how many device=");
        debug.println(NumOfFound,DEC);
        for( int i = 0 ; i < NumOfFound ; i ++ ) {
            reply = btComm.readString() ;
            replyPrint("name", reply);
            termName.entry( reply ) ;
        }
        reply = btComm.readString() ;//"Inquiry Done"の分を空読み
        replyPrint("done", reply);
        for( int i = 0 ; i < NumOfFound ; i ++ ) {
            btComm.print("sr,");
            btComm.println( termName.get( i ) );
            reply = btComm.readString() ;//"AOK"の分を空読み
            replyPrint("sr",reply);
        }
    }
    for( int i = 0 ; i < 0 ; i ++ ) {//空読み
        reply = btComm.readString() ;
        debug.print("reply:");
    }
}
void replyPrint( char* title, char* str )
{
    debug.print(title);
    debug.print(" ");
    replyPrint(str);
}
void replyPrint( char* str )
{
    debug.print("reply[");
    debug.print(str);
    debug.println( "]" ) ;
}
int strCmp( char* dist, char* src )
{
    int len = strlen( src ) ;
    return strncmp( dist, src, len ) ;
}

