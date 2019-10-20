package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ACTIVITY_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTACT_ID;

import java.util.function.Predicate;

import seedu.address.commons.core.Messages;
import seedu.address.model.Model;
import seedu.address.model.activity.Activity;
import seedu.address.model.person.Person;
import seedu.address.logic.parser.Prefix;

/**
 * Finds and lists all persons in address book whose name contains any of the argument keywords.
 * Keyword matching is case insensitive.
 */
public class ViewCommand extends Command {

    public static final String COMMAND_WORD = "view";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds either an activity or contact with "
            + "the specified ID number and displays them. ID must be numerical!\n" + "Parameters: {"
            + PREFIX_ACTIVITY_ID + " | " + PREFIX_CONTACT_ID + "}ID\n" +
            "Example: " + COMMAND_WORD + "a/2";

    private final int id;
    private final Prefix prefix;

    public ViewCommand(int id, Prefix prefix) {
        this.id = id;
        this.prefix = prefix;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        Predicate<Person> predicate;
        if (prefix.equals(PREFIX_ACTIVITY_ID)) {
            Activity a = model.getActivityBook().getActivity(id);
            if (a == null) {
                return new CommandResult(
                        String.format(Messages.MESSAGE_ACTIVITY_NONEXISTENT, id));
            }
            predicate = (Person x) -> a.hasPerson(x.getPrimaryKey());
        } else if (prefix.equals(PREFIX_CONTACT_ID)) {
            predicate = (Person x) -> x.getPrimaryKey() == id;
        } else {
            predicate = x -> true; // default to showing everyone if no args
        }

        model.updateFilteredPersonList(predicate, model);
        return new CommandResult(
                String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, model.getFilteredPersonList().size()));
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand)) {
            return false;
        }

        // state check
        ViewCommand v = (ViewCommand) other;

        return id == v.id && prefix.equals(v.prefix);
    }
}
