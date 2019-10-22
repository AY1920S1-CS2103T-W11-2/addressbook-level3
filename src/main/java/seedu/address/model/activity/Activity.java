package seedu.address.model.activity;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;

import seedu.address.model.activity.exceptions.PersonNotInActivityException;
import seedu.address.model.person.Person;

/**
 * Represents an Activity class containing participants ID and expenses.
 */
public class Activity {

    private static int primaryKeyCounter;
    private final int primaryKey;
    private final Title title;
    private final ArrayList<Expense> expenses;

    // Id, Balance, Transfers arrays are supposed to be one-to-one.
    private final ArrayList<Integer> participantIds;
    private final ArrayList<Double> participantBalances;
    // Each [i][j] entry with value E means i owes j -E amount.
    // The actual personid has to be obtained from the id array, and i, j just
    // represent the indices in that array where you can find them.
    private final ArrayList<ArrayList<Double>> transferMatrix;
    private final ArrayList<ArrayList<Double>> debtMatrix;

    /**
     * Constructor for Activity.
     * @param primaryKey The primary key of this activity.
     * @param title Title of the activity.
     * @param ids The people participating in the activity.
     */
    public Activity(int primaryKey, Title title, Integer ... ids) {
        requireAllNonNull(title);
        participantIds = new ArrayList<>();
        expenses = new ArrayList<>();
        participantBalances = new ArrayList<>();
        transferMatrix = new ArrayList<>();
        debtMatrix = new ArrayList<>();
        this.primaryKey = primaryKey;
        this.title = title;
        for (Integer id : ids) {
            participantIds.add(id);
            participantBalances.add(0.0);
            transferMatrix.add(new ArrayList<>(Collections.nCopies(ids.length, 0.0)));
            debtMatrix.add(new ArrayList<>(Collections.nCopies(ids.length, 0.0)));
        }
    }
    /**
     * Constructor for Activity.
     * @param title Title of the activity.
     * @param ids The people participating in the activity.
     */
    public Activity(Title title, Integer ... ids) {
        requireAllNonNull(title);
        participantIds = new ArrayList<>();
        expenses = new ArrayList<>();
        participantBalances = new ArrayList<>();
        transferMatrix = new ArrayList<>();
        debtMatrix = new ArrayList<>();
        this.primaryKey = primaryKeyCounter++;
        this.title = title;
        for (Integer id : ids) {
            participantIds.add(id);
            participantBalances.add(0.0);
            transferMatrix.add(new ArrayList<>(Collections.nCopies(ids.length, 0.0)));
            debtMatrix.add(new ArrayList<>(Collections.nCopies(ids.length, 0.0)));
        }
    }

    public int getPrimaryKey() {
        return primaryKey;
    }

    public static int getPrimaryKeyCounter() {
        return primaryKeyCounter;
    }

    public static void setPrimaryKeyCounter(int pk) {
        primaryKeyCounter = pk;
    }

    /**
     * Gets the list of id of participants in the activity.
     * @return An ArrayList containing the id participants.
     */
    public ArrayList<Integer> getParticipantIds() {
        return participantIds;
    }

    /**
     * Gets the list of expenses in the activity.
     * @return An ArrayList of expenses.
     */
    public ArrayList<Expense> getExpenses() {
        return expenses;
    }

    /**
     * Gets the name of the activity.
     * @return A String representation of the name of the activity.
     */
    public Title getTitle() {
        return title;
    }

    /**
     * Gets the transfer matrix.
     * @return The matrix. Every (i, j) entry reflects how much i receives from
     * j. Negative amounts means i has to give j money.
     *
     */
    public ArrayList<ArrayList<Double>> getTransferMatrix() {
        simplifyExpenses();
        return transferMatrix;
    }

    /**
     * Invite people to the activity.
     * @param people The people that will be added into the activity.
     */
    public void invite(Person ... people) {
        invite(Stream.of(people)
                .mapToInt(x -> x.getPrimaryKey())
                .toArray());
    }

    /**
     * Invite people to the activity.
     * @param primaryKeys The primary keys of the people that will be added
     * into the activity.
     */
    public void invite(int ... primaryKeys) {
        int len = participantIds.size();
        int newlen = len + primaryKeys.length;
        for (int i = 0; i < primaryKeys.length; i++) {
            int p = primaryKeys[i];
            if (!participantIds.contains(p)) {
                participantIds.add(p);
                participantBalances.add(0.0); // newcomers don't owe.
                for (int j = 0; j < len; j++) {
                    debtMatrix.get(j).add(0.0); // extend columns
                    transferMatrix.get(j).add(0.0); // extend columns
                }
                debtMatrix.add(new ArrayList<>(Collections.nCopies(newlen, 0.0)));
                transferMatrix.add(new ArrayList<>(Collections.nCopies(newlen, 0.0)));
            }
        }
    }

    /**
     * Checks whether the person with ID is present in this activity.
     * @param personId Id of the person to check.
     * @return True if person exists, false otherwise.
     */
    public boolean hasPerson(Integer personId) {
        return participantIds.contains(personId);
    }

    /**
     * Remove people from the activity
     * @param people The people that will be removed from the activity.
     */
    public void disinvite(Person ... people) {
        // haven't implemented what if list does not contain that specific person
        // TODO: also care about transfermatrix and debtmatrix
        // perhaps this? public void disinvite(Integer personId)
    }

    /**
     * Add expense to the activity
     * @param expenditures The expense(s) to be added.
     * @throws PersonNotInActivityException if any person is not found
     */
    public void addExpense(Expense ... expenditures) throws PersonNotInActivityException {
        boolean allPresent = Stream.of(expenditures)
                .map(x -> x.getPersonId())
                .allMatch(x -> participantIds.contains(x));
        if (!allPresent) {
            throw new PersonNotInActivityException();
        }
        for (Expense expense : expenditures) {
            expenses.add(expense);

            // We update the balance sheet
            int payer = expense.getPersonId();
            double amount = expense.getAmount().value;
            double splitAmount = amount / participantIds.size();
            int i = 0;
            for (; i < participantIds.size(); i++) {
                if (participantIds.get(i) == payer) {
                    break;
                }
            }

            for (int j = 0; j < debtMatrix.size(); j++) {
                if (j != i) {
                    debtMatrix.get(j).set(i, debtMatrix.get(j).get(i) + splitAmount);
                }
            }
        }
    }

    /**
     * Simplifies the expenses in the balance sheet and also updates transferMatrix.
     * See: https://pure.tue.nl/ws/portalfiles/portal/2062204/623903.pdf
     */
    private void simplifyExpenses() {
        int i = 0;
        int j = 0;
        int n = participantBalances.size();

        // negative balance means you lent more than you borrowed.
        for (int a = 0; a < debtMatrix.size(); a++) {
            double acc = 0;
            for (int b = 0; b < debtMatrix.size(); b++) {
                acc += debtMatrix.get(a).get(b);
                acc -= debtMatrix.get(b).get(a);
                transferMatrix.get(a).set(b, 0.0);
            }
            participantBalances.set(a, acc);
        }

        while (i != n && j != n) {
            double bi;
            double bj;
            if ((bi = participantBalances.get(i)) <= 0) {
                i++;
                continue;
            } else if ((bj = participantBalances.get(j)) >= 0) {
                j++;
                continue;
            }

            double m = bi < -bj ? bi : -bj;
            // i gives j $m.
            transferMatrix.get(i).set(j, transferMatrix.get(i).get(j) - m);
            transferMatrix.get(j).set(i, transferMatrix.get(j).get(i) + m);
            participantBalances.set(i, bi - m);
            participantBalances.set(j, bj + m);
        }
        System.out.println("Transfer matrix:");
        for (ArrayList<Double> a : transferMatrix) {
            System.out.println(a.toString());
        }
    }

    /**
     * Soft deletes an expense within an activity
     * @param positions The 0-indexed expense number to delete
     */
    public void deleteExpense(int ... positions) {
        for (int i = 0; i < positions.length; i++) {
            if (positions[i] > 0 && positions[i] <= expenses.size()) {
                expenses.get(positions[i] - 1).delete();
            } // if beyond range not implemented yet
        }
    }

    @Override
    public String toString() {
        return String.format("Activity \"%s\"\n", title);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(title, participantIds, expenses);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Activity)) {
            return false;
        }

        Activity otherActivity = (Activity) other;
        return otherActivity.getTitle().equals(getTitle())
                && otherActivity.getParticipantIds().equals(getParticipantIds())
                && otherActivity.getExpenses().equals(getExpenses());
    }
}
