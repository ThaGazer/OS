/*
 *Author: Justin Ritter
 *File: trithread.c
 *
 *Description: find all right triangles for a set of x and y coordinates
 */

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <semaphore.h>
#include <pthread.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <string.h>

/*
 * a structure to hold x and y coordinates
 */
typedef struct point {
  int x, y;
} Point;

/*
 * a structure to represent a triangle of three points
 */
typedef struct triangle {
  Point p1, p2, p3;
} Triangle;

typedef struct parameters {
	pthread_t tid;
  int start;
  int stop;
	Point* pointList;
	int totalPoints;
	Triangle* foundTriangles;
	int* ftLoc;
	int* rightTriangles;
	sem_t* lock;
} Params;

/*
 * checks if two points are equal to each other
 */
int point_equals(Point p1, Point p2) {
    return p1.x == p2.x && p1.y == p2.y;
}

/*
 * a swap function
 */
void max(int* x, int* y) {
  int temp;
  if(*y > *x) {
    temp = *x;
    *x = *y;
    *y = temp;
  }
}

/*
 * a check to see if all sides of a triangle are legal
 */
int distanceCheck(Triangle t) {
  int a,b,c;
  c = abs(((t.p1.x - t.p2.x)*(t.p1.x - t.p2.x)) +
	  ((t.p1.y - t.p2.y)*(t.p1.y - t.p2.y)));

  a = abs(((t.p1.x - t.p3.x)*(t.p1.x - t.p3.x)) +
	  ((t.p1.y - t.p3.y)*(t.p1.y - t.p3.y)));
  max(&c, &a);

  b = abs(((t.p2.x - t.p3.x)*(t.p2.x - t.p3.x)) +
	  ((t.p2.y - t.p3.y)*(t.p2.y - t.p3.y)));
  max(&c, &b);

  return (a+b) == c;
}

/*
 * a triangle edge case check
 */
int isZeroSlope(Triangle t) {
  return ((t.p1.x == t.p2.x) && (t.p1.x == t.p3.x)) ||
	  ((t.p1.y == t.p2.y) && (t.p1.y == t.p3.y));
}

/*
 * a check to see if the points form an actual triangel
 */
int isTriangle(Triangle t) {
  if(point_equals(t.p1, t.p2) || point_equals(t.p1,t.p3) ||
      point_equals(t.p2,t.p3) || isZeroSlope(t)) {
    return 0;
  }
  return 1;
}

/*
 * a check to see if a triangle forms a right triangle
 */
int isRight(Triangle t) {
  if(isTriangle(t)) {
    return distanceCheck(t);
  } else {
    return 0;
  }
}

/*
 * checks if a triangle contains the point p
 */
int tri_has(Point p, Triangle t) {
  if(point_equals(t.p1,p) || point_equals(t.p2, p)
		  || point_equals(t.p3, p)) {
    return 1;
  }
  return 0;
}

/*
 * if a list of triangles contains t
 */
int contains(Triangle t, Triangle* list) {
  int length = sizeof(list)/sizeof(list[0]);

  for(int i = 0; i < length; i++) {
    if(tri_has(t.p1, list[i]) && tri_has(t.p2, list[i]) &&
        tri_has(t.p3, list[i])) {
      if(tri_has(list[i].p1, t) && tri_has(list[i].p2, t) &&
		      tri_has(list[i].p3, t)) {
        return 1;
      }
    }
  }
  return 0;
}

/*
 * format a triangle to print
 */
void tprint(Triangle t) {
   printf("(%d %d)(%d %d)(%d %d)\n",
		   t.p1.x,t.p1.y,t.p2.x,t.p2.y,t.p3.x,t.p3.y);
}

/*
 * reads fully from file descriptor
 */
void readf(int desc, void* buff, size_t size) {
  size_t count = 0;
  ssize_t rcvd;

  while(count < size) {
    rcvd = read(desc, ((char*) buff) + count, size-count);

    if(rcvd < 0) {
      perror("read() failed");
      exit(1);
    } else if(rcvd == 0) {
      exit(-1);
    }
    count += (size_t)rcvd;
  }
}

/*
 * main function for threads to run
 */
static void* findTriangle(void* parameters) {
  Params* param = (Params*)parameters;

  for(int j = param->start; j < param->stop; j++) {
    for(int k = j+1; k < param->totalPoints; k++) {
      for(int l = k+1; l < param->totalPoints; l++) {
        Triangle t;
        t.p1 = param->pointList[j];
        t.p2 = param->pointList[k];
        t.p3 = param->pointList[l];

        //add locks
        sem_wait(param->lock);
        if(!contains(t, param->foundTriangles)) {
          sem_post(param->lock);
          if(isRight(t)) {
            sem_wait(param->lock);
            *param->rightTriangles += 1;
            param->foundTriangles[*param->ftLoc] = t;
            *param->ftLoc += 1;
            sem_post(param->lock);
          }
        }
      }
    }
  }
  return NULL;
}

int main(int argc, char** argv) {

  //check parameter requirements
  if(argc != 3) {
    fprintf(stderr, "Usage: %s <fileName> <nprocs>\n", argv[0]);
    exit(1);
  }

  char* fname = argv[1];
  int nprocs = atoi(argv[2]);
  int totalRight = 0;
  sem_t lock;

  //init locks
  sem_init(&lock, 1,1);

  //reads from fname and stores points in pointList
  FILE* fileId;
  if(!(fileId = fopen(fname, "r"))) {
    perror("could not open file\n");
    exit(1);
  }

  //reads number of points in file
  int totalPoints = 0;
  fscanf(fileId, "%d", &totalPoints);
  if(totalPoints == 0) {
    fprintf(stderr, "incorrect file formatting: lead number is bad");
    exit(1);
  }
  fgetc(fileId);

  //reads and stores all the points in the file
  Point* pointList = (Point*) malloc(totalPoints * sizeof(Point));
  int xCoord, yCoord;
  for(int i = 0; i < totalPoints; i++) {

    if(fscanf(fileId, "%d %d", &xCoord, &yCoord) == EOF) {
      fprintf(stderr, "incorrect file formatting\n");
      exit(1);
    }

    pointList[i].x = xCoord;
    pointList[i].y = yCoord;
  }

  //close file pointer
  fclose(fileId);

  //calculate the total number of triangle that can should be made
  int totalLoad = ((totalPoints * totalPoints * totalPoints) / 6) - (totalPoints / 6);

  //nprocs bounds check
  if(nprocs > totalPoints) {
    fprintf(stderr, "to many proccesses to be spawned\n");
    exit(1);
  }

  //a list of all triangles that have already been checked
  Triangle* foundTriangles = (Triangle*) malloc(totalLoad * sizeof(Triangle));
  int ftLoc = 0;

  int workLoad = totalPoints / nprocs;
  int remainder = totalPoints % nprocs;
  int beg = 0, end = workLoad;
  Params procParams[nprocs];

  //creates child processes
  for(int i = 0; i < nprocs; i++) {
    //set thread parameters
    procParams[i].start = beg;
    procParams[i].stop = end;
    procParams[i].pointList = pointList;
    procParams[i].totalPoints = totalPoints;
    procParams[i].foundTriangles = foundTriangles;
    procParams[i].ftLoc = &ftLoc;
    procParams[i].rightTriangles = &totalRight;
    procParams[i].lock = &lock;

    //replace with pthread_init
    pthread_create(&procParams[i].tid, NULL, *findTriangle, &procParams[i]);

    beg += workLoad;
    end += workLoad;

    if(remainder > 0) {
      end++;
      remainder--;
    }
  }

  //replace with pthread_join
  for(int i = 0; i < nprocs; i++) {
    if(pthread_join(procParams[i].tid, NULL)) {
      fprintf(stderr, "Error joing thread");
      exit(1);
    }
  }

  //the sum of all right triangles found
  printf("%d\n", totalRight);

  //free file mem
  free(pointList);
  free(foundTriangles);

  return 0;
}
