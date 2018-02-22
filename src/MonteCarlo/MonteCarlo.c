/*
 *Author: Justin Ritter
 *File: MonteCarlo.c
 *
 *Description: The Monte Carlo method of approzimating pie 
 */

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <string.h>
#include <math.h>

int main(int argc, char** argv) {

  //check parameter requirements
  if(argc != 3) {
    fprintf(stderr, "incorrect number of parameters:%d\n", argc-1); 
    return -1;
  }

  int centX = 0, centY = 0, rad = 1;
  char* fname = argv[1];
  int nProc = atoi(argv[2]);
  int fd[nProc][2];
  int amount[nProc];
  int startPos[nProc];
  int totPnts;
  char** line;

  //parents pid
  pid_t Ppid = getpid();
  pid_t Cpid;

  //file checker
  FILE* fileId;
  if(!(fileId = fopen(fname, "r"))) {
    perror("could not open file");
    return -1;
  }

  //calculates how much each child should handle
  fscanf(fileId, "%d", &totPnts);
  fgetc(fileId);
  int amnt = totPnts/nProc;

  int sum = 0, strtPos = 0;
  for(int i = 0; i < nProc; i++) {
    amount[i] = amnt;
    startPos[i] = strtPos;
    strtPos += amnt;
    sum += amnt;
  }
  //adds extra points to last process
  if(sum < totPnts) {
    amount[nProc-1] += (totPnts - sum);
  }

  //reads and stores the file
  line = malloc(totPnts * sizeof(char*));
  int c;
  for(int i = 0; i < totPnts; i++) {
    line[i] = malloc(80 * sizeof(char));

    int j;
    for(j = 0;(c = fgetc(fileId)) != '\n'; j++) {
      if(c == EOF) {
        perror("wrong number of points");
        exit(-1);
      }
      line[i][j] = c;
    }
    line[i][j] = '\n';
  }

  //close file pointer
  fclose(fileId);

  //creates child processes
  for(int i = 0; i < nProc; i++) {
    if(Ppid == getpid()) {
      pipe(fd[i]);
      write(fd[i][1], &amount[i], sizeof(int));
      write(fd[i][1], &startPos[i], sizeof(int));

      if((Cpid = fork()) == -1) {
        fprintf(stderr, "fork failed: %d\n", getpid());
        return -1;
      }
    }
    if(Cpid == 0) { 
      int howmuch, where;
      read(fd[i][0], &howmuch, sizeof(int));
      read(fd[i][0], &where, sizeof(int));

      int count;
      for(int i = where; i < (howmuch+where); i++) {
        double x = atof(strtok(line[i], ", "));
        double y = atof(strtok(NULL, "\n"));

        if((sqrt(x - centX) + sqrt(y - centY)) <= sqrt(rad)) {
          count++; 
        }
      } 
      return 0;
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

  double pi = 4 * (totPntIn/totPnts);
  printf("pi is equal to: %f\n", pi);

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
