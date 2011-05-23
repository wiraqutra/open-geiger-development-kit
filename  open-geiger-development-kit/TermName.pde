#include "TermName.h"


TermName::TermName()
{
    init();
}
void TermName::init()
{
    m_NumOfTerm = 0 ;
    m_TermName[0] = m_Term0 ;
    m_TermName[1] = m_Term1 ;
    m_TermName[2] = m_Term2 ;
    m_TermName[3] = m_Term3 ;
    m_TermName[4] = m_Term4 ;
    m_TermName[5] = m_Term5 ;
    m_TermName[6] = m_Term6 ;
    m_TermName[7] = m_Term7 ;
}
bool TermName::entry( char* name )
{
    if( name == NULL ) {
        return false ;
    }
    char* commnaPnt = strchr( name, ',' ) ;
    if( commnaPnt != NULL ) {
        *commnaPnt = 0 ;
    }
    if( m_NumOfTerm >= TermName_TERMINAL_MAX ) {
        return false ;
    }
    strcpy( m_TermName[ m_NumOfTerm ], name ) ;
    m_NumOfTerm ++ ;
    return true ;
}
char* TermName::get( int number )
{
    if( number >= m_NumOfTerm  ) {
        return( NULL ) ;
    }
    return( m_TermName[ number ] ) ;
}
