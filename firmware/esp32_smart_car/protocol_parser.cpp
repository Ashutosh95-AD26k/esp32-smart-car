#include "protocol_parser.h"

ParsedCommand ProtocolParser::parse(const String& line) {
    ParsedCommand result = { CMD_NONE, 0 };
    String trimmed = line;
    trimmed.trim();

    if (trimmed.length() == 0) {
        return result;
    }

    char firstChar = trimmed.charAt(0);

    switch (firstChar) {
        case 'F':
        case 'f':
            result.type = CMD_FORWARD;
            break;
        case 'B':
        case 'b':
            result.type = CMD_BACKWARD;
            break;
        case 'L':
        case 'l':
            result.type = CMD_LEFT;
            break;
        case 'R':
        case 'r':
            result.type = CMD_RIGHT;
            break;
        case 'S':
        case 's':
            result.type = CMD_STOP;
            break;
        case 'X':
        case 'x':
            result.type = CMD_EMERGENCY_STOP;
            break;
        case 'H':
        case 'h':
            result.type = CMD_HEARTBEAT;
            break;
        case 'V':
        case 'v': {
            if (trimmed.length() > 1) {
                int val = trimmed.substring(1).toInt();
                result.type = CMD_SET_SPEED;
                result.value = constrain(val, 0, 255);
            }
            break;
        }
        default:
            result.type = CMD_NONE;
            break;
    }

    return result;
}
