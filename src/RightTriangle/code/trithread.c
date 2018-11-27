/*
 * Author: Justin Ritter
 * File: trithread.c
 * Date: 11/20/2018
 */

#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <sys/mman.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <fcntl.h>

typedef struct point {
  int x,y;
} Point;

typedef struct thread_arguments {
  Point* points;
  int pointCount, workLoad, index;
  int rightTriangles;
} arg_t;

/*
  calculates the distance between two points
*/
double point_distance(Point p1, Point p2) {
  return abs(((p1.x - p2.x) * (p1.x - p2.x)) + ((p1.y - p2.y) * (p1.y - p2.y)));
}

/*
  the work that each thread will do
*/
void* threadWork(void* pass_args) {
  arg_t* args = (arg_t*)pass_args;
  Point* points = args->points;
  int size = args->pointCount;
  int workLoad = args->workLoad;
  int index = args->index;

  for(int i = (workLoad*index); i < ((workLoad*index) + workLoad); i++) {
    for(int j = i+1; j < size-1; j++) {
      for(int k = j+1; k < size; k++) {
        double a = point_distance(points[i], points[j]);
        double b = point_distance(points[i], points[k]);
        double c = point_distance(points[j], points[k]);

        if(a+b == c || a+c == b || b+c == a) {
          args->rightTriangles++;
        }
      }
    }
  }
  return NULL;
}

/*
  spawns threads to do work
*/
int findRightTriangle
    (Point* pointList, int pointCount, char* tCount) {
  int threadCount = atoi(tCount);
  int sum = 0;
  if(threadCount > pointCount-2) {
    threadCount = pointCount-2;
  }

  if(pointCount >= 3) {
    pthread_t threads[threadCount];
    arg_t threadArgs[threadCount];
    for(int i = 0; i < threadCount; i++) {
      threadArgs[i].rightTriangles = 0;
      threadArgs[i].points = pointList;
      threadArgs[i].pointCount = pointCount;
      threadArgs[i].workLoad = (pointCount/threadCount);
      threadArgs[i].index = i;

      if(pthread_create(&threads[i], NULL, threadWork, (void*)&threadArgs[i])) {
        perror("could not create thread\n");
        exit(1);
      }
    }

    for(int i = 0; i < threadCount; i++) {
      if(pthread_join(threads[i], NULL)) {
        perror("could not join threads\n");
        exit(1);
      }

      sum += threadArgs[i].rightTriangles;
    }
  }

  return sum;
}

/*
  find next int a char buffer
*/
int nextInt(char* buff, int* pos) {
  char c;
  int i = 0;
  while((c = buff[*pos]) != ' ' && c != '\n') {
    i = (c-'0')+i*10;;
    (*pos)++;
  }
  (*pos)++;

  return i;
}

/*
  reads points in from the file
*/
void readPoints(Point** points, int* pointCount, char* filename) {
  int fd;
  struct stat st; //file info
  void* mp; //mapped buff

  //open file
  if((fd = open(filename,O_RDONLY)) < 0) {
    perror("could not open file");
    exit(1);
  }
  
  //file statistics
  if(fstat(fd, &st) < 0) {
    perror("fstat failed\n");
    exit(1);
  }

  //memory map the file
  if(!(mp = mmap(NULL, st.st_size, PROT_READ, MAP_PRIVATE, fd, 0))) {
    perror("mapping failed\n");
    exit(1);
  }

  int readPos = 0;
  *pointCount = nextInt(mp, &readPos);
  *points = malloc(*pointCount * sizeof(Point));

  for(int i = 0; i < *pointCount; i++) {
    (*points)[i].x = nextInt(mp, &readPos);
    (*points)[i].y = nextInt(mp, &readPos);
  }

  munmap(mp, st.st_size);
  close(fd);
}

int main(int argc, char*argv[]) {
  if(argc < 3 || argc > 3) {
    fprintf(stderr, "Usage: %s <filename> <threadCount>\n", argv[0]);
    exit(1);
  }

  Point* pointList = NULL;
  int pointCount = 0;

  readPoints(&pointList, &pointCount, argv[1]);

  fprintf(stdout, "Right triangles: %d\n", findRightTriangle(pointList, pointCount, argv[2]));

  free(pointList);

  return 0;
}
