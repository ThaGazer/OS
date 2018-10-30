#ifndef PARLOR_H
#define PARLOR_H

#include <string>
#include <queue>
#include <stdexcept>
#include <semaphore.h>
using namespace std;

/**
 * Class to manage a fortune teller parlor.
 * \version 1.0
 */
class Parlor
{
  private:
  sem_t parlorLock;
  sem_t closeLock;
  sem_t tellerLock;
  bool closed;
  int capacity;
  queue<string> que;

  /**
   * A flag for is the parlor has been closed or not 
   */
  bool isClosed();

  /**
   * Sets the parlor to be closed
   */ 
  void setClosed();

  public:
    /**
     * Construct parlor for maximum given patrons
     * \param maxPatrons max patrons in parlor (non-negative)
     * \throws invalid_argument if maxPatrons < 0
     */
    Parlor(const int maxPatrons = 5);

    /**
     * Free any parlor resources
     */
    ~Parlor();

    /**
     * Called by fortune teller to request next patron in order by arrival.
     * This method will block until a patron is available.  A patron
     * getting her fortune told gives up her parlor chair.  If the Parlor
     * is closed, this method will return null.
     * \return name of patron who fortune is told
     * \throws runtime_error if Parlor closed
     */
    string TellFortune();

    /**
     * Adds a new patron with given name to the parlor.  The parlor has a finite
     * number of chairs (given to the constructor).  If there is no available chair
     * in the parlor, this method immediately returns false; otherwise, this method
     * immediately returns true. This method immediately returns false when the shop
     * is closed.
     * \param name name of the patron
     * \return true if patron was added or false if the parlor was full or closed
     */
    bool NewPatron(const string& name);

    /**
     * This function marks the parlor as closed, causing threads blocked in
     * tellFortune() and newPatron() to return with false/null.  Threads
     * subsequently calling these function also immediately return with false/null.
     */
    void Close();
};

#endif