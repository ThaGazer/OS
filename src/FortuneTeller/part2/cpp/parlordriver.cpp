//TODO this
/*
 * Author: Justin Ritter
 * File: parlordriver.cpp
 * Date: 10/11/2018
 * 
 * Description: a class to run and manage a parlor
 */

#include "parlor.h"
#include <pthread.h>
#include <stdio.h>

void* runTeller(void*);
void* patronSpawner(void*);
void* runPatron(void*);

int main(int argc, char* argv[]) {

    pthread_t thread[2];
    Parlor* parlor = new Parlor();

    //spawns teller thread
    pthread_create(&thread[0], NULL, &runTeller, &parlor);
    //spawns patron thread
    pthread_create(&thread[1], NULL, &patronSpawner, &parlor);

    for(int i = 0; i < 2; i++) {
        pthread_join(thread[i], NULL);
    }

    return 0;
}

void* runTeller(void* par) {
   Parlor* parlor = (Parlor*)par;
   int peopleHelped = 0;

    while(true) {
        string patron = parlor->TellFortune();

        if(patron.empty()) {
            printf("Parlor is closed for the day");
            break;
        } else {
            printf("Fortune teller telling %s", patron.c_str());
            peopleHelped++;
        }
    }

    printf("Patrons helped for the day %i", peopleHelped);
    exit(0);
}

void* patronSpawner(void* parlor) {
    int NUMTHREAD
    pthread_t thread[NUMTHREAD];

    for(int i = 0; i < NUMTHREAD; i++) {
        pthread_create(&thread[i], NULL, &runPatron, parlor);
    }

    for(int i = 0; i < NUMTHREAD; i++) {
        pthread_join(thread[i], NULL);
    }
}

void* runPatron(void* par) {
    Parlor* parlor = (Parlor*)par;

    if(parlor->NewPatron(to_string(i))) {
        printf("%i took a seat in the parlor", i);
    } else {
        printf("%i continued on to find a new store", i);
    }
}