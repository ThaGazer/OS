/*
 * Author: Justin Ritter
 * File: parlor.cpp
 * Date: 10/10.2018
 * 
 * Description: implemenation of function found in parlor.h
 */

#include "parlor.h";

Parlor::Parlor(int maxPatrons) {
    if(maxPatrons < 0) {
        throw illegal_argument("cannot have negative seats in the parlor");
    }

    closed = false;
    capacity = maxPatrons;

    //set semaphores
    sem_init(parlorLock, 0, 1);
    sem_init(closeLock, 0, 1);
    sem_init(tellerLock, 0, 1);
}

Parlor::~Parlor() {
    sem_destroy(parlorLock);
    sem_destroy(closeLock);
    sem_destroy(tellerLock);
    capacity = 0;
}

string Parlor::TellFortune() {
    if(!isClosed()) {
        while(que.size() <= capacity) {
            if(isClosed()) {
                return NULL;
            }
            sem_wait(tellerLock);
        }

        sem_wait(parlorLock);
        string name = que.front();
        que.pop();
        sem_post(parlorLock);

        return name;
    } else {
        throw runtime_error("Parlor is closed for the day");
    }
}

bool Parlor::NewPatron(string& name) {
    if(!isClosed()) {
        //TODO empty name check?

        sem_wait(parlorLock);
        bool ret = false;
        if(que.size() < capacity) {
            que.push(name);
            ret = true;
            sem_post(tellerLock);
        }
        sem_post(parlorLock);
        return ret;
    } else {
        return false;
    }

}

void Parlor::Close() {
    sem_wait(closeLock);
    setClosed();
    sem_post(tellerLock);
    sem_post(closeLock);
}

bool Parlor::isClosed() {
    sem_wait(closeLock);
    bool ret = closed;
    sem_post(closeLock);
    return ret;
}

void Parlor::setClosed() {
    closed = true;
}