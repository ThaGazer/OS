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

/*
 * a structure to hold x and y coordinates
 */
struct point {
  int x, y;
} typedef Point;

/*
 * a structure to represent a triangle of three points
 */
struct triangle {
  Point p1, p2, p3;
} typedef Triangle;

/*
 * checks if two points are equal to each other
 */
bool point_equals(Point p1, Point p2) {
    return p1.x == p2.x && p1.y == p2.y;
}

/*
 * a swap function
 */
void max(int &x, int &y) {
  int temp;
  if(y > x) {
    temp = x;
    x = y;
    y = temp;
  }
}

/*
 * a check to see if all sides of a triangle are legal
 */
bool distanceCheck(Triangle t) {
  int a,b,c;
  c = ((t.p1.x - t.p2.x)*(t.p1.x - t.p2.x)) + ((t.p1.y - t.p2.y)*(t.p1.y - t.p2.y));

  a = ((t.p1.x - t.p3.x)*(t.p1.x - t.p3.x)) + ((t.p1.y - t.p3.y)*(t.p1.y - t.p3.y));
  max(c,a);

  b = ((t.p2.x - t.p3.x)*(t.p2.x - t.p3.x)) + ((t.p2.y - t.p3.y)*(t.p2.y - t.p3.y));
  max(c,b);

  return (a+b) == c;
}

/*
 * a triangle edge case check
 */
bool isZeroSlope(Triangle t) {
  return t.p1.x == t.p2.x && t.p1.x == t.p3.x || t.p1.y == t.p2.y && t.p1.y == t.p3.y;
}

/*
 * a check to see if the points form an actual triangel
 */
bool isTriangle(Triangle t) {
  if(point_equals(t.p1, t.p2) || point_equals(t.p1,t.p3) ||
      point_equals(t.p2,t.p3) || isZeroSlope(t)) {
    return false;
  }
  return true;
}

/*
 * a check to see if a triangle forms a right triangle
 */
bool isRight(Triangle t) {
  if(isTriangle(t)) {
    return distanceCheck(t);
  }
}

/*
 * checks if a triangle contains the point p
 */
bool tri_has(Point p, Triangle t) {
  if(point_equals(t.p1,p) || point_equals(t.p2, p) || point_equals(t.p3, p)) {
    return true;
  }
  return false;
}

/*
 * if a list of triangles contains t
 */
bool contains(Triangle t, Triangle* list) {
  int length = sizeof(list)/sizeof(list[0]);

  for(int i = 0; i < length; i++) {
    if(tri_has(t.p1, list[i]) && tri_has(t.p2, list[i]) &&
        tri_has(t.p3, list[i])) {
      if(tri_has(list[i].p1, t) && tri_has(list[i].p2, t) && tri_has(list[i].p3, t)) {
        return true;
      }
    }
  }
  return false;
}

/*
 * format a triangle to print
 */
void tprint(Triangle t) {
   printf("(%d %d)(%d %d)(%d %d)\n",t.p1.x,t.p1.y,t.p2.x,t.p2.y,t.p3.x,t.p3.y);
}

int main(int argc, char** argv) {

  //check parameter requirements
  if(argc != 3) {
    fprintf(stderr, "Usage: <fileName> <nprocs>\n");
    return -1;
  }

  char* fname = argv[1];
  int nprocs = atoi(argv[2]);
  int fd[nprocs*2][2];
  int totalPoints = 0;

  //pids
  pid_t pid;

  //file checker
  FILE* fileId;
  if(!(fileId = fopen(fname, "r"))) {
    perror("could not open file\n");
    return -1;
  }

  //reads number of points in file
  fscanf(fileId, "%d", &totalPoints);
  fgetc(fileId);

  //reads and stores all the points in the file
  Point* pointList = (Point*) malloc(totalPoints * sizeof(Point));
  int xCoord, yCoord, space;
  for(int i = 0; i < totalPoints; i++) {

    if(fscanf(fileId, "%d %d", &xCoord, &yCoord) == EOF) {
      perror("incorrect file formatting\n");
      exit(-1);
    }
//  printf("%d. %d %d\n", i, xCoord, yCoord);
    pointList[i].x = xCoord;
    pointList[i].y = yCoord;
  }

  //close file pointer
  fclose(fileId);

  //calculate the total number of triangle that can should be made
  int totalLoad = ((totalPoints * totalPoints * totalPoints) / 6) - (totalPoints / 6);


  //nprocs bounds check
  if(nprocs > totalPoints) {
    perror("to many proccesses to be spawned");
    exit(-1);
  }

  //a list of all triangles that have already been checked
  Triangle* foundTriangles = (Triangle*) malloc(totalLoad * sizeof(Triangle));
  int ftLoc = 0;

  //creates child processes
  for(int i = 0; i < nprocs; i++) {
    if((pid = fork()) == -1) {
      fprintf(stderr,"fork failed: %d\n", getpid());
    }

    //in child process
    if(pid == 0) {
      printf("child: %d", getpid());

      int beg, end;
      int rightTri = 0;

      read(fd[i][0], &beg, sizeof(int));
      read(fd[i][0], &end, sizeof(int));

      for(int j = beg; j < end; j++) {
        for(int k = j+1; k < totalPoints; k++) {
          for(int l = k; l < totalPoints; l++) {
            Triangle t;
            t.p1 = pointList[j];
            t.p2 = pointList[k];
            t.p3 = pointList[l];

            //add locks
            if(!contains(t, foundTriangles)) {
              if(isRight(t)) {
                rightTri++;
                foundTriangles[ftLoc] = t;
                ftLoc++;
              }
            }
          }
        }
      }

      write(fd[i+nprocs][1], &rightTri, sizeof(int));
    } else {
      printf("parent");

      int workLoad = totalPoints / nprocs;
      int remainder = totalPoints % nprocs;
      int beg = 0, end = workLoad;

      //write the beginning position
      write(fd[i][1], &beg, sizeof(int));

      //write the last position to read
      write(fd[i][1], &end, sizeof(int));

      beg += workLoad;
      end += workLoad;

      if(remainder > 0) {
        end++;
        remainder--;
      }
    }
  }

  //waiting for children to finish
  while(wait(NULL));

  //summing calculations from the children processes
  int sum = 0;
  for(int i = 0; i < nprocs; i++) {
    int pntIn;
    int byteRead = 0;
    read(fd[i+nprocs][0], &pntIn, sizeof(int));
    sum += pntIn;
  }
  printf("%d", sum);

  //free file mem
  free(pointList);
  free(foundTriangles);

  //close pipe
  for(int i = 0; i < nprocs; i++) {
    for(int j = 0; j < 2; j++) {
      close(fd[i][j]);
    }
  }

  return 0;
}
