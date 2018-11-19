/*
 * Author: Justin Ritter
 * Date: 11/13/2018
 * File: triangles.cpp
 *
 */

#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include <fcntl.h>
#include <iostream>
#include <sstream>
#include <vector>
#include <thread>
#include <algorithm>
#include <atomic>
#include <string>

using namespace std;

class Point {
private:
  int x,y;

public:
  Point(int x, int y) {
    this->x = x;
    this->y = y;
  }

  int getX() {
    return x;
  }

  int getY() {
    return y;
  }

  double distance(Point p) {
    return ((p.getX() - x) * (p.getX() - x)) + 
	    ((p.getY() - y) * (p.getY() - y));
  }

  bool equals(Point p) {
    return x == p.getX() && y == p.getY();
  }

  string toString() {
    stringstream stream;
    stream << "(" << x << "," << y << ")";
    return stream.str();
  }

  bool operator<(Point p) {
    if(x = p.x) {
      return y < p.getY();
    }
    return x < p.getX();
  }
};



int nextInt(char* buff, int &pos) {
  string ret;
  char c;
  while((c = buff[pos]) != ' ' && c != '\n') {
    ret += c;
    pos++;
  }
  pos++;
   
  return stoi(ret);
}

void* readPoints(vector<Point> &pList, char* args) {
  int fd;
  struct stat st;
  void* mp;

  if((fd = open(args, O_RDONLY)) < 0) {
    perror("Could not open file\n");
    exit(1);
  }

  if(fstat(fd, &st)) {
    perror("fstat Failed\n");
    exit(1);
  }

  if(!(mp = mmap(NULL, st.st_size, PROT_READ, MAP_SHARED, fd, 0))) {
    perror("Mapping failed");
    exit(1);
  }

  char* intMap = (char*)mp;
  int readPos = 0;
 
  int numPoints;
  try {
    numPoints = nextInt(intMap, readPos);
  } catch(const invalid_argument &ia) {
    cerr << "Reading points: could not read point count" << endl;
    exit(1);
  }

  if(numPoints <= 0) {
    fprintf(stderr, "Buffer underflow: error number of points: %d", numPoints);
    exit(1);
  }

  for(int i = 0; i < numPoints; i++) {
    int x,y;
    try {
      x = nextInt(intMap, readPos);
      y = nextInt(intMap, readPos);
    } catch(const invalid_argument &ia) {
      cerr << "Reading points: point format" << x << " " << y << endl;
      exit(1);
    }

    Point tmp(x,y);
    pList.push_back(tmp);
  }
}

bool rightCheck(Point p1, Point p2, Point p3) {
  double a,b,c, tmp;

  a = p1.distance(p2);
  b = p1.distance(p3);
  c = p2.distance(p3);

  if(a > c) {
    swap(a, c);
  }
  if(b > c) {
    swap(a, c);
  }

  return (a+b) == c;
}

void threadedTriangle(atomic<int>& foundTri, int work, int location, vector<Point> pointList) {
  for(int i = location; i < (location + work) && i < pointList.size(); i++) {
    Point iPoint = pointList[i];

    for(int j = 0; j+1 < i; j++) {
      if(rightCheck(iPoint, pointList[j], pointList[j+1])) {
        ++foundTri;
      }
    }

    for(int j = i+1; j < (pointList.size()-1); j++) {
      if(rightCheck(iPoint, pointList[j], pointList[j+1])) {
        ++foundTri;
      }
    }

    //edge cases
    if(i == 0) {
      if(rightCheck(iPoint, pointList[i+1], pointList[pointList.size()-1])) {
        ++foundTri;
      }
    } else if(i == pointList.size()-1) {
      if(rightCheck(iPoint, pointList[0], pointList[i-1])) {
        ++foundTri;
      }
    } else {
      if(rightCheck(iPoint, pointList[i-1], pointList[i+1])) {
        ++foundTri;
      }
    }
  }
}

int findRightTriangles(vector<Point> pointList, char* args) {
  int threadCount;
  try {
    threadCount = stoi(args);

  } catch(const invalid_argument &ia) {
    perror("unrecognized number of threads");
    exit(1);
  }

  vector<thread> threads;
  atomic<int> rightTriangle(0);
  int workLoad = pointList.size() / threadCount;
  int remainder = pointList.size() % threadCount;
  int startLoc = 0;

  for(int i = 0; i < threadCount; i++) {
    int adjWorkLoad = workLoad;
    if(remainder > 0) {
      adjWorkLoad++;
      remainder--;
    }

    threads.push_back(thread(threadedTriangle, ref(rightTriangle), 
			    adjWorkLoad, startLoc, pointList));

    startLoc += adjWorkLoad;
  }

  for(auto& t : threads) {
    t.join();
  }

  return rightTriangle.load();
}

int main(int argc, char* argv[]) {
  if(argc < 3 || argc > 3) {
    fprintf(stderr, "Usage: %s <filename> <thread count>\n", argv[0]);
    exit(1);
  }

  vector<Point> pointList;

  readPoints(pointList, argv[1]);
  for(Point p : pointList) {
    cout << p.toString();
  }
  cout << endl;

  printf("%d\n", findRightTriangles(pointList, argv[2]));

  return 0;
}