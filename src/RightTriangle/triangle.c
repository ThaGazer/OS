/*
 *Author: Justin Ritter
 *File: triangles.c
 *
 *Description: find all right triangles for a set of x and y coordinates
 */

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <semaphore.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <string.h>

struct point {
  int x, y;
} typedef Point

struct triangle {
  Point p1, p2, p3;
} typedef Triangle

bool isRight(Triangle t);
bool contains(Triangle t, Triangle* list);

int main(int argc, char** argv) {

  //check parameter requirements
  if(argc != 3) {
    fprintf(stderr, "Usage: <fileName> <nprocs>", argc-1);
    return -1;
  }

  char* fname = argv[1];
  int nprocs = atoi(argv[2]);
  int fd[nprocs][2];
  int totalPoints;

  //pids
  pid_t Ppid = getpid();
  pid_t Cpid;

  //file checker
  FILE* fileId;
  if(!(fileId = fopen(fname, "r"))) {
    perror("could not open file\n");
    return -1;
  }

  //calculates how much each child should handle
  fscanf(fileId, "%d", &totalPoints);
  fgetc(fileId);

  //reads and stores all the points in the file
  Point* pointList = malloc(totalPoints * sizeof(Point));
  int xCoord, yCoord, space;
  for(int i = 0; i < totalPoints; i++) {
    xCoord = fgetc(fileId);
    space = fgetc(fileId);
    yCoord = fgetc(fileId);

    if(xCoord != '\n' && yCoord != '\n' && space != '\n') {
      pointList[i].x = atoi(xCoord);
      pointList[i].y = atoi(yCoord);
    } else {
      perror("incorrect file formatting\n");
    }
  }

  //close file pointer
  fclose(fileId);

  int totalLoad = ((totalPoints * totalPoints * totalPoints) / 6) - (totalPoints / 6);
  int workLoad = totalLoad/nProc;
  Triangle foundTriangles[] = new malloc((totalPoints*2) * sizeof(Triangle));

  //creates child processes
  for(int i = 0; i < nProc; i++) {
    //in parent process
    if(Ppid == getpid()) {

      //creates pipe
      pipe(fd[i]);

      //what to send to child

      if((Cpid = fork()) == -1) {
        fprintf(stderr, "fork failed: %d\n", getpid());
        return -1;
      }
    }
    //in child process
    if(Cpid == 0) { 

      int i,j,k, amount;
      bool start = true;
      for(; i < totalPoints && amount > 0; i++) {
        if(!start) {
          j = i+1;
        }
        for(; j < totalPoints && amount > 0; j++) {
          if(!start) {
            k = j;
          }
          for(; k < totalPoints && amount > 0; k++) {
            if(start) {
              start = false;
            }

            Point p1 = pointList[i], p2 = pointList[j], p3 = pointList[k];
            if(contains(p1,p2,p3, foundTriangles) && isRight(p1,p2,p3)) {
            }

          }
        }
      }
    }    
  }

  //waiting for children to finish
  pid_t wpid;
  int status;
  while((wpid = wait(&status)) > 0) {
  }

  //summing calculations from the children processes
  int totPntIn;
  for(int i = 0; i < nProc; i++) {
    int pntIn;
    read(fd[i][1], &pntIn, sizeof(int));
    totPntIn += pntIn;
  }

  //free file mem
  if(line) {
    for(int i = 0; i < totPnts; i++) {
      free(line[i]);
    }
    free(line);
  }

  //close pipe
  for(int i = 0; i < nProc; i++) {
    for(int j = 0; j < 2; j++) {
      close(fd[i][j]);
    }
  }

  return 0;
}
