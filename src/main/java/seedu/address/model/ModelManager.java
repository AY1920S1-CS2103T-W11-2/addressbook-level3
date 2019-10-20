package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.activity.Activity;
import seedu.address.model.person.NameContainsAllKeywordsPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final ActivityBook activityBook;
    private final UserPrefs userPrefs;
    private final InternalState internalState;
    private final FilteredList<Person> filteredPersons;
    private Context context;

    /**
     * Initializes a ModelManager with the given addressBook, userPrefs and internalState.
     */
    public ModelManager(
            ReadOnlyAddressBook addressBook,
            ReadOnlyUserPrefs userPrefs,
            InternalState internalState,
            ActivityBook activityBook) {
        super();
        requireAllNonNull(addressBook, userPrefs, activityBook);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        this.activityBook = new ActivityBook(activityBook);
        this.internalState = new InternalState(internalState);
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
        context = new Context();
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs(), new InternalState(), new ActivityBook());
    }

    //=========== Context ====================================================
    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public Context getContext() {
        return context;
    }

    //=========== UserPrefs ====================================================

    @Override
    public void setInternalState(InternalState internalState) {
        requireNonNull(internalState);
        this.internalState.updateInternalState(internalState);
    }

    @Override
    public InternalState getInternalState() {
        return internalState;
    }

    //=========== UserPrefs ====================================================

    @Override
    public void setUserPrefs(ReadOnlyUserPrefs userPrefs) {
        requireNonNull(userPrefs);
        this.userPrefs.resetData(userPrefs);
    }

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    @Override
    public Path getAddressBookFilePath() {
        return userPrefs.getAddressBookFilePath();
    }

    @Override
    public void setAddressBookFilePath(Path addressBookFilePath) {
        requireNonNull(addressBookFilePath);
        userPrefs.setAddressBookFilePath(addressBookFilePath);
    }

    @Override
    public void setActivityBookFilePath(Path activityBookFilePath) {
        requireNonNull(activityBookFilePath);
        userPrefs.setActivityBookFilePath(activityBookFilePath);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return addressBook.hasPerson(person);
    }

    @Override
    public ArrayList<Person> findPersonAny(NameContainsKeywordsPredicate predicate) {
        requireNonNull(predicate);
        return addressBook.findPerson(predicate);
    }

    @Override
    public ArrayList<Person> findPersonAll(NameContainsAllKeywordsPredicate predicate) {
        requireNonNull(predicate);
        return addressBook.findPerson(predicate);
    }

    @Override
    public void deletePerson(Person target) {
        addressBook.removePerson(target);
    }

    @Override
    public void addPerson(Person person) {
        addressBook.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        addressBook.setPerson(target, editedPerson);
    }

    //=========== ActivityBook================== =============================================================

    @Override
    public Path getActivityBookFilePath() {
        return userPrefs.getActivityBookFilePath();
    }

    @Override
    public void setActivityBook(ActivityBook activityBook) {
        this.activityBook.resetData(activityBook);
    }

    @Override
    public ActivityBook getActivityBook() {
        return activityBook;
    }

    @Override
    public void deleteActivity(Activity target) {
        activityBook.removeActivity(target);
    }

    @Override
    public void addActivity(Activity activity) {
        activityBook.addActivity(activity);
    }

    @Override
    public void setActivity(Activity target, Activity editedActivity) {
        requireAllNonNull(target, editedActivity);

        activityBook.setActivity(target, editedActivity);
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code versionedAddressBook}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return filteredPersons;
    }

    /**
     * Actually filters the whole list and returns a list of the result.
     * @return An array of {@code Person} which pass the predicate of the filtered list.
     */
    public Person[] filteredPersonArr() {
        ArrayList<Person> res = new ArrayList<>(filteredPersons.size());
        for (Person p : filteredPersons) {
            res.add(p);
        }
        return res.toArray(new Person[res.size()]);
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate, Model model) {
        updateFilteredPersonList(predicate);
        model.setContext(new Context(filteredPersonArr()));
    }

    @Override
    public boolean equals(Object obj) {
        // short circuit if same object
        if (obj == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(obj instanceof ModelManager)) {
            return false;
        }

        // state check
        ModelManager other = (ModelManager) obj;
        return addressBook.equals(other.addressBook)
                && activityBook.equals(other.activityBook)
                && userPrefs.equals(other.userPrefs)
                && internalState.equals(other.internalState)
                && filteredPersons.equals(other.filteredPersons);
    }

}
