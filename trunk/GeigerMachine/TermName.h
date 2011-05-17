#ifndef TermName_h
#define TermName_h

//#include <inttypes.h>
//#include "WProgram.h"

#define TermName_TERMINAL_MAX 8
#define TermName_TERMINAL_STR_MAX 13
class TermName
{
 private:
    char* m_TermName[ TermName_TERMINAL_MAX ] ;
    char m_Term0[TermName_TERMINAL_STR_MAX] ;
    char m_Term1[TermName_TERMINAL_STR_MAX] ;
    char m_Term2[TermName_TERMINAL_STR_MAX] ;
    char m_Term3[TermName_TERMINAL_STR_MAX] ;
    char m_Term4[TermName_TERMINAL_STR_MAX] ;
    char m_Term5[TermName_TERMINAL_STR_MAX] ;
    char m_Term6[TermName_TERMINAL_STR_MAX] ;
    char m_Term7[TermName_TERMINAL_STR_MAX] ;
    int m_NumOfTerm ;
 public:
    TermName();
    bool entry(char* name ) ;
    void init();
    char* get(int number);
};

#endif

