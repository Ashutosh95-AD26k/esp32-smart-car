#ifndef PROTOCOL_PARSER_H
#define PROTOCOL_PARSER_H

#include <Arduino.h>

enum CommandType {
    CMD_NONE,
    CMD_FORWARD,
    CMD_BACKWARD,
    CMD_LEFT,
    CMD_RIGHT,
    CMD_STOP,
    CMD_EMERGENCY_STOP,
    CMD_SET_SPEED,
    CMD_HEARTBEAT
};

struct ParsedCommand {
    CommandType type;
    int value; // Used for speed (0-255)
};

class ProtocolParser {
public:
    static ParsedCommand parse(const String& line);
};

#endif // PROTOCOL_PARSER_H
