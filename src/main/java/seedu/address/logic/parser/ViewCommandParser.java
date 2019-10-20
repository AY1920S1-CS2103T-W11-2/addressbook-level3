package seedu.address.logic.parser;

import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ACTIVITY_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTACT_ID;

import java.lang.NumberFormatException;
import java.util.List;

import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class ViewCommandParser implements Parser<ViewCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public ViewCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_ACTIVITY_ID, PREFIX_CONTACT_ID);
        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE));
        }

        List<String> activityIds = argMultimap.getAllValues(PREFIX_ACTIVITY_ID);
        List<String> contactIds = argMultimap.getAllValues(PREFIX_CONTACT_ID);
        int actsize = activityIds.size();
        int consize = contactIds.size();
        if (actsize + consize != 1) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE));
        }

        try {
            if (actsize == 1) {
                return new ViewCommand(Integer.parseInt(activityIds.get(0)), PREFIX_ACTIVITY_ID);
            } else {
                return new ViewCommand(Integer.parseInt(contactIds.get(0)), PREFIX_CONTACT_ID);
            }
        } catch (NumberFormatException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE));
        }
    }

}
