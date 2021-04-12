/* compile & run : g++ -Wall -Wextra -Wconversion -ansi -Wpedantic -std=gnu++11 prisme.cpp -o prisme -lpthread && ./prisme 8080
 * test (in a second terminal) : nc 127.0.0.1 8080
 */

#include <iostream>
#include <cstdlib>
#include <string>
#include <fstream>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <thread>
#include <mutex>		// std::mutex, std::unique_lock
#include <vector>

// READ :
//  https://www.geeksforgeeks.org/socket-programming-cc/
//  https://www.cplusplus.com/reference/thread/thread/

std::mutex mtx_main;		// this will allow a passive lock for main

std::mutex mtx_clients;
std::vector < std::pair < std::thread, int >> clients;

bool
 first = false;

void deal_with_socket(uint16_t port);
void deal_with_client(int socket, unsigned int id);

int main(int argc, char *argv[])
{

	if (argc - 1 != 1) {
		std::cerr << "Usage: " << argv[0] << " <PORT>" << std::endl;
		return EXIT_FAILURE;
	}

    // TODO : check that's a real number...
    // WARNING this must be a uint16_t (aka <65536)
    int port = atoi(argv[1]);

    // from here ....
	// SOCKET STUFFS...
	std::thread connection_dealer =
	    std::thread(deal_with_socket, (uint16_t) port);

	// wait for a "double client" problem...
	connection_dealer.join();
    // ... to there we may place the whole deal_with_socket code... eventually...

	mtx_clients.lock();
    for (auto & i:clients) {
        // detach the running thread for the last client to prevent "terminate exception" when main will stop
		i.first.detach();
		close(i.second);
	}
	mtx_clients.unlock();

	return 0;
}

void deal_with_socket(uint16_t port)	// this thread will wait 'forever' for new connexions...
{
	int server_fd;
	struct sockaddr_in address;
	int opt = 1;
	int addrlen = sizeof(address);

	// Creating socket file descriptor 
	if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0) {
		std::cerr << "socket failed " << __FILE__ << " " << __LINE__ <<
		    std::endl;
		exit(EXIT_FAILURE);
	}

	// Forcefully attaching socket to the port "port" 
	if (setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR | SO_REUSEPORT,
		       &opt, sizeof(opt))) {
		std::
		    cerr << "setsockopt" << __FILE__ << " " << __LINE__ << std::
		    endl;
		exit(EXIT_FAILURE);
	}
	address.sin_family = AF_INET;
	address.sin_addr.s_addr = INADDR_ANY;
	address.sin_port = htons(port);

	if (bind(server_fd, (struct sockaddr *)&address, sizeof(address)) < 0) {
		std::
		    cerr << "bind failed " << __FILE__ << " " << __LINE__ <<
		    std::endl;
		exit(EXIT_FAILURE);
	}

	if (listen(server_fd, 3) < 0) {
		std::cerr << "listen " << __FILE__ << " " << __LINE__ << std::
		    endl;
		exit(EXIT_FAILURE);
	}

	while (true) {
		int
		 new_socket;

		// A NEW CLIENT CONNECTION... PASSIVE WAIT
		if ((new_socket = accept(server_fd, (struct sockaddr *)&address,
					 (socklen_t *) & addrlen)) < 0) {
			std::
			    cerr << "accept " << __FILE__ << " " << __LINE__ <<
			    std::endl;
			exit(EXIT_FAILURE);
		}

		// check boolean before starting the new thread...
		mtx_clients.lock();
		if (!first) {
			first = true;
		} else {
			// this is bad...
			std::cerr <<
			    "            .-'''-.        .-'''-.                     "
			    << std::endl;
			std::cerr <<
			    "           '   _    \\     '   _    \\                   "
			    << std::endl;
			std::cerr <<
			    "/|       /   /` '.   \\  /   /` '.   \\  __  __   ___    "
			    << std::endl;
			std::cerr <<
			    "||      .   |     \\  ' .   |     \\  ' |  |/  `.'   `.  "
			    << std::endl;
			std::cerr <<
			    "||      |   '      |  '|   '      |  '|   .-.  .-.   ' "
			    << std::endl;
			std::cerr <<
			    "||  __  \\    \\     / / \\    \\     / / |  |  |  |  |  | "
			    << std::endl;
			std::cerr <<
			    "||/'__ '.`.   ` ..' /   `.   ` ..' /  |  |  |  |  |  | "
			    << std::endl;
			std::cerr <<
			    "|:/`  '. '  '-...-'`       '-...-'`   |  |  |  |  |  | "
			    << std::endl;
			std::cerr <<
			    "||     | |                            |  |  |  |  |  | "
			    << std::endl;
			std::cerr <<
			    "||\\    / '                            |__|  |__|  |__| "
			    << std::endl;
			std::cerr <<
			    "|/\'..' /                                              "
			    << std::endl;
			std::cerr <<
			    "'  `'-'`                                               "
			    << std::endl;

			mtx_clients.unlock();	// clean ressources currently allocated
			close(new_socket);
			goto this_is_bad;	// and fail...
		}

		// keep the thread into the vector to avoid the call to thread destructor when we leave the current block
		clients.emplace_back(std::thread(deal_with_client, new_socket, clients.size()), new_socket);	// emplace_back creates the std::pair...
		mtx_clients.unlock();
	}

 this_is_bad:
	;
}

#define N_CHAR 1024UL

void deal_with_client(int socket, unsigned int id)	// this threads will gets message from their own client (client id == id)
{
	char buffer[N_CHAR];
	size_t valread;
	bool is_end = false;
	std::cout << "Connection started for client " << id << std::endl;

	do {
		valread = read(socket, buffer, N_CHAR);	// PASSIVE
		buffer[valread - 1] = '\0';	// FIXME : buffer overflow may occurs

		std::string message(buffer);
		if (message.compare(0, sizeof("END"), "END") == 0) {	// when we have the stop message
			is_end = true;
		} else {	// else we echo the message...
			std::string msg = std::to_string(id) + " " + buffer;
			send(socket, msg.c_str(), msg.length(), 0);
			std::cout << msg << std::endl;
		}

	}
	while (!(is_end || valread == 0));	// is_end occurs when we got the stop message, valread==0 when the socket is closed

	mtx_clients.lock();
	first = false;
	mtx_clients.unlock();

	std::cout << "Connection ended for client " << id << std::endl;
	close(socket);
}
