#include <iostream>
#include <fstream>
#include <string>
#include <stdlib.h>

#define min(a, b) ((a) < (b) ? (a) : (b))

using namespace std;

int main(int argc, char **argv) {
	string s;
	long long bytesToWrite, bytesInLoop;
	char buffer[100];
	if (argc != 3 && argc != 5) {
		printf("Usage: FileCutter <FileName> <Bytes> (optional) -o <outFile>\n");
		return 1;
	}
	ifstream inFile(argv[1], ios::binary);
	ofstream outFile;

	bytesToWrite = atoll(argv[2]);
	inFile.seekg(0, ios::end);
	if (inFile.tellg() <= bytesToWrite) {
		printf("The Filesize is only %d bytes. Choose a value below that.\n", inFile.tellg());
		return 1;
	}
	inFile.seekg(0, ios::beg);
	if (argc == 5) {
		outFile.open(argv[4], ios::binary | ios::trunc);
	}
	else {
		s.assign(argv[1]);
		s.insert(s.find_last_of("."), " - cutted");
		printf("Writing into File %s...\n", s.c_str());
		outFile.open(s.c_str(), ios::binary);
	}
	do {
		bytesInLoop = min(100ll, bytesToWrite);
		inFile.read(buffer, bytesInLoop);
		outFile.write(buffer, bytesInLoop);
		bytesToWrite -= bytesInLoop;
	} while (bytesToWrite > 0);

	inFile.close();
	outFile.close();
	printf("Writing finished.\n");
	return 0;
}