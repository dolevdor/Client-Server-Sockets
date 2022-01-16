#include <stdlib.h>
#include "../include/connectionHandler.h"
#include <iostream>
#include <thread>
#include <vector>

short bytesToShort(char bytes[2]);

void ErrorHandler(short opcode);

void AckHandler(short opcode, ConnectionHandler &handler, bool &isLogout);

void NotificationHandler(short post, ConnectionHandler &connectionHandler);

void shortToBytes(short num, char* bytesArr);


using namespace std;

class Read {
private:
    ConnectionHandler &connectionHandler;
    bool& isLogout;
    bool& errorLogout;

public:
    Read(ConnectionHandler &connectionHandler, bool &isLogout, bool &errorLogout):connectionHandler(connectionHandler),isLogout(isLogout),
    errorLogout(errorLogout){}

    void reader() {
        while (!isLogout) {
            errorLogout = false;
            char bytes[2];
            char postOrPm[1];
            connectionHandler.getBytes(bytes, 2);
            short opcode = bytesToShort(bytes);
            if (opcode == 11) {
                connectionHandler.getBytes(bytes, 2);
                short messageOpcode = bytesToShort(bytes);
                ErrorHandler(messageOpcode);
            } else if (opcode == 10) {
                connectionHandler.getBytes(bytes, 2);
                short messageOpcode = bytesToShort(bytes);
                AckHandler(messageOpcode, connectionHandler, isLogout);
            } else if (opcode == 9) {
                connectionHandler.getBytes(postOrPm, 1);
                char pmOrPost = postOrPm[0]; // CHECK IF IT WORKS
                string kind;
                if (pmOrPost == '\0')
                    kind = "Public ";
                else
                    kind = "PM ";
//                cout << "im going to notification handler: "<< opcode<<endl;
                NotificationHandler(kind, connectionHandler);
                opcode = 0;
            }


        }
        connectionHandler.close();
    }

    void NotificationHandler(string &kind, ConnectionHandler &connectionHandler) {
        string postingUser;
        connectionHandler.getLine(postingUser);//gets the posting user
        string Content;
        connectionHandler.getLine(Content);
        cout << "NOTIFICATION " << kind << postingUser << " " << Content << endl;
    }

    void AckHandler(short opcode, ConnectionHandler &connectionHandler, bool &isLogout) {
        switch ((int)opcode) {
            case 1: // Register
                cout << "ACK 1" << endl;
                break;
            case 2: // Login
                cout << "ACK 2" << endl;
                break;
            case 3: // Logout
            {
                cout << "ACK 3" << endl;
                isLogout = true;
                break;
            }
            case 4: // Follow
                cout << "ACK 4" << endl;
                break;
            case 5: // Post
                cout << "ACK 5" << endl;
                break;
            case 6: // PM
                cout << "ACK 6" << endl;
                break;
            case 7: { // Logstat
                char bytes[2];
                connectionHandler.getBytes(bytes, 2);
                short age = bytesToShort(bytes);
                connectionHandler.getBytes(bytes, 2);
                short NumPosts = bytesToShort(bytes);
                connectionHandler.getBytes(bytes, 2);
                short NumFollowers = bytesToShort(bytes);
                connectionHandler.getBytes(bytes, 2);
                short NumFollowing = bytesToShort(bytes);
                cout << "ACK 7 " << age << " " << NumPosts << " " << NumFollowers << " " << NumFollowing << endl;
                break;
            }
            case 8: // Stat
            {
                char bytes[2];
                connectionHandler.getBytes(bytes, 2);
                short age = bytesToShort(bytes);
                connectionHandler.getBytes(bytes, 2);
                short NumPosts = bytesToShort(bytes);
                connectionHandler.getBytes(bytes, 2);
                short NumFollowers = bytesToShort(bytes);
                connectionHandler.getBytes(bytes, 2);
                short NumFollowing = bytesToShort(bytes);
                cout << "ACK 8 " << age << " " << NumPosts << " " << NumFollowers << " " << NumFollowing << endl;
                break;
            }
            case 12: // Block
                cout << "ACK 12" << endl;
        }
    }

    short bytesToShort(char bytesArr[]) {
        short result = (short) ((bytesArr[0] & 0xff) << 8);
        result += (short) (bytesArr[1] & 0xff);
//        cout<<"bytesToShort: "<< result<<endl;
        return result;

    }

    void ErrorHandler(short opcode) {
        switch ((int)opcode) {
            case 1: // Register
                cout << "ERROR 1" << endl;
                break;
            case 2: // Login
                cout << "ERROR 2" << endl;
                break;
            case 3: // Logout
                cout<<"I entered"<<endl;
                errorLogout = true;
                cout << "ERROR 3" << endl;
                break;
            case 4: // Follow
                cout << "ERROR 4" << endl;
                break;
            case 5: // Post
                cout << "ERROR 5" << endl;
                break;
            case 6: // PM
                cout << "ERROR 6" << endl;
                break;
            case 7: // Logstat
                cout << "ERROR 7" << endl;
                break;
            case 8: // Stat
                cout << "ERROR 8" << endl;
                break;
            case 12: // Block
                cout << "ERROR 9" << endl;
                break;
        }
    }
};



class Write {
private:
    ConnectionHandler &connectionHandler;
    bool &isLogout;
    bool &errorLogout;
public:
    Write(ConnectionHandler &connectionHandler, bool &isLogout, bool &errorLogout) : connectionHandler(connectionHandler),
                                                                  isLogout(isLogout), errorLogout(errorLogout) {}
    void writer() {
        while (!isLogout) {
            errorLogout = false;
            const short bufsize = 1024;
            char buf[bufsize];
            std::cin.getline(buf, bufsize);
            std::string line(buf);
            int len = line.length();
            int lenTemp = line.length();
            string contentForPost = "";
            string contentForPM = "";
            char ch[] = {';'};

            vector<string> lineVector;
            char opcodeBytesArr[2];


            while (lenTemp > 0) {
                int i = line.find_first_of(' ');
                if ((lineVector.size() == 1) && (lineVector[0] == "POST")) {
                    contentForPost = contentForPost + line;
                    //cout<<"content: "<<contentForPost<<endl;
                    break; // No need to push any more words to the vector
                }
                if ((lineVector.size() == 2) && (lineVector[0] == "PM")) {
                    contentForPM = contentForPM + line;
                    break; // No need to push any more words to the vector
                }

                if (i == -1) {
                    lineVector.push_back(line);
                    lenTemp = 0;
                } else {
                    string word = line.substr(0, i);
                    lineVector.push_back(word);
                    line = line.substr(i + 1, len);
                    lenTemp = line.length();
                }
            }

            if ("REGISTER" == lineVector[0]) {
                shortToBytes(1, opcodeBytesArr);
                connectionHandler.sendBytes(opcodeBytesArr, 2);
                string userName = lineVector[1];
                connectionHandler.sendLine(userName);
                string password = lineVector[2];
                connectionHandler.sendLine(password);
                string birthday = lineVector[3];
                connectionHandler.sendLine(birthday);
                connectionHandler.sendBytes(ch, 1);
            }

            if ("LOGIN" == lineVector[0]) {

                shortToBytes(2, opcodeBytesArr);
                connectionHandler.sendBytes(opcodeBytesArr, 2);
                string userName = lineVector[1];
                connectionHandler.sendLine(userName);
                string password = lineVector[2];
                connectionHandler.sendLine(password);
                const char *str = lineVector[3].c_str();
                connectionHandler.sendBytes(str,1);
                connectionHandler.sendBytes(ch, 1);
            }

            if ("LOGOUT" == lineVector[0]) {
                shortToBytes(3, opcodeBytesArr);
                connectionHandler.sendBytes(opcodeBytesArr, 2);
                connectionHandler.sendBytes(ch, 1);
                while (!isLogout && !errorLogout) {
                }
            }

            if ("FOLLOW" == lineVector[0]) {
                shortToBytes(4, opcodeBytesArr);
                connectionHandler.sendBytes(opcodeBytesArr, 2);

                char followBytesArr[1];
                if (lineVector[1] == "0")
                    followBytesArr[0] = 0;
                else
                    followBytesArr[0] = 1;
                connectionHandler.sendBytes(followBytesArr, 1);

                string userName = lineVector[2];
                connectionHandler.sendLine(userName);
                connectionHandler.sendBytes(ch, 1);
            }

            if ("POST" == lineVector[0]) {
                shortToBytes(5, opcodeBytesArr);
                connectionHandler.sendBytes(opcodeBytesArr, 2);
                connectionHandler.sendLine(contentForPost);
                connectionHandler.sendBytes(ch, 1);
            }

            if ("PM" == lineVector[0]) {
                shortToBytes(6, opcodeBytesArr);
                connectionHandler.sendBytes(opcodeBytesArr, 2);
                string userName = lineVector[1];
                connectionHandler.sendLine(userName);
                connectionHandler.sendLine(contentForPM);
                connectionHandler.sendBytes(ch, 1);
            }

            if ("LOGSTAT" == lineVector[0]) {
                shortToBytes(7, opcodeBytesArr);
                connectionHandler.sendBytes(opcodeBytesArr, 2);
                connectionHandler.sendBytes(ch, 1);
            }

            if ("STAT" == lineVector[0]) {
                shortToBytes(8, opcodeBytesArr);
                connectionHandler.sendBytes(opcodeBytesArr, 2);
                string userNameList = lineVector[1];
                connectionHandler.sendLine(userNameList);
                connectionHandler.sendBytes(ch, 1);
            }

            if ("BLOCK" == lineVector[0]) {
                shortToBytes(12, opcodeBytesArr);
                connectionHandler.sendBytes(opcodeBytesArr, 2);
                string userName = lineVector[1];
                connectionHandler.sendLine(userName);
                connectionHandler.sendBytes(ch, 1);
            }



        }
    }
};

void shortToBytes(short num, char* bytesArr)
{
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}

int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);

    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    bool isLogOut = false;
    bool errorLogout = false;

    Read socketReader(connectionHandler,isLogOut, errorLogout);
    Write socketWrite(connectionHandler,isLogOut, errorLogout);

    thread t1(&Read::reader, &socketReader);
    thread t2(&Write::writer, socketWrite);

    t1.join();
    t2.join();

    return 0;
}

