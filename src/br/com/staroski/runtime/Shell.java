package br.com.staroski.runtime;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Utility class to siplify the executions of command lines, shellscripts or other programs on OS.<BR>
 * The {@link #Command(String, String...)} constructor receives the command line and his optional parameters.<BR>
 * 
 * @author Ricardo Artur Staroski
 */
public final class Shell {

    private final List<String> command = new LinkedList<>();

    private final StringBuilder output = new StringBuilder();
    private final StringBuilder error = new StringBuilder();
    private ShellListener outputListener;

    private ShellListener errorListener;
    private PrintWriter writer;

    /**
     * Creates a new instance of {@link Shell}.
     * 
     * @param exec
     *            The command, shelscript or program to be executed.
     * @param params
     *            The parameters (Optional)
     */
    public Shell(String exec, String... params) {
        addOutputListener(text -> output.append(text));
        addErrorListener(text -> error.append(text));
        addParam(exec, params);
    }

    /**
     * Adds an {@link ShellListener} to this {@link Shell} to receive the error messages from the underlying process.
     * 
     * @param errorListener
     *            The error listener to be added.
     * 
     * @see #removeErrorListener(ShellListener)
     */
    public void addErrorListener(ShellListener errorListener) {
        this.errorListener = ShellListeners.add(this.errorListener, errorListener);
    }

    /**
     * Adds an {@link ShellListener} to receive the output messages from the underlying process.
     * 
     * @param outputListener
     *            The output listener to be added.
     */
    public void addOutputListener(ShellListener outputListener) {
        this.outputListener = ShellListeners.add(this.outputListener, outputListener);
    }

    /**
     * Allows to add more parameters to this {@link Shell}.
     * 
     * @param first
     *            The first parameter to be added.
     * 
     * @param others
     *            The other parameters to be added (Optional).
     */
    public void addParam(String first, String... others) {
        command.add(first);
        for (String other : others) {
            command.add(other);
        }
    }

    /**
     * Allows to add more parameters to this {@link Shell}.
     * 
     * @param params
     *            The parameters to be added.
     */
    public void addParam(String[] params) {
        for (String param : params) {
            command.add(param);
        }
    }

    /**
     * Executes this {@link Shell}.
     * 
     * @return The exit code of the process.
     * 
     * @throws IOException
     *             If something goes wrong.
     */
    public int execute() throws IOException {
        return execute(null);
    }

    /**
     * Allows to add more parameters to this {@link Shell}.
     * 
     * @param directory
     *            The directory from where to run this {@link Shell}.
     * 
     * @return The exit code of the process.
     * 
     * @throws IOException
     *             If something goes wrong.
     */
    public int execute(File directory) throws IOException {
        try {
            Process process = executeAssinchronous(directory);
            return process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Executes this {@link Shell} in assynchronous way returning the underlying {@link Process}.<br>
     * In this case it's responsibility of the developer to call the {@link Process#waitFor()} method to get the exit code of the process.
     * 
     * 
     * @return The {@link Process} created.
     * 
     * @throws IOException
     *             If something goes wrong.
     */
    public Process executeAssinchronous() throws IOException {
        return executeAssinchronous(null);
    }

    /**
     * Executes this {@link Shell} in assynchronous way returning the underlying {@link Process}.<br>
     * In this case it's responsibility of the developer to call the {@link Process#waitFor()} method to get the exit code of the process.
     * 
     * @param directory
     *            The directory from where to run this {@link Shell}.
     * 
     * @return The {@link Process} created.
     * 
     * @throws IOException
     *             If something goes wrong.
     */
    public Process executeAssinchronous(File directory) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(directory);
        Process process = builder.start();

        Thread errorsReader = new Thread(() -> readStream(process.getErrorStream(), () -> errorListener));
        errorsReader.setDaemon(true);

        Thread outputReader = new Thread(() -> readStream(process.getInputStream(), () -> outputListener));
        outputReader.setDaemon(true);

        writer = new PrintWriter(new BufferedOutputStream(process.getOutputStream()));

        errorsReader.start();
        outputReader.start();

        return process;
    }

    /**
     * Returns a {@link String} containing the underlying {@link Process}'s error messages.
     * 
     * @return The underlying {@link Process}'s error messages.
     */
    public String getError() {
        return error.toString();
    }

    /**
     * Gets an {@link PrintWriter PrintWriter} to allow writing into the process input.<BR>
     * It only makes sense to use the {@link PrintWriter PrintWriter} for processes that were started using the {@link #executeAssinchronous()} or
     * {@link #executeAssinchronous(File)} methods.
     * 
     * @return The {@link PrintWriter PrintWriter} for writing on the process input.
     * 
     * @throws IllegalStateException
     *             If the process was not yet started.
     */
    public PrintWriter getInput() {
        if (writer == null) {
            throw new IllegalStateException("Process not yet started!");
        }
        return writer;
    }

    /**
     * Returns a {@link String} containing the underlying {@link Process}'s output messages.
     * 
     * @return The underlying {@link Process}'s output messages.
     */
    public String getOutput() {
        return output.toString();
    }

    /**
     * Checks if there are error messages on the underlying process.
     * 
     * @return <code>true</code> if there are error messages and <code>false</code> otherwise.
     */
    public boolean hasError() {
        return !getError().isEmpty();
    }

    /**
     * Checks if there are output messages on the underlying process.
     * 
     * @return <code>true</code> if there are output messages and <code>false</code> otherwise.
     */
    public boolean hasOutput() {
        return !getOutput().isEmpty();
    }

    /**
     * Removes an {@link ShellListener} from this {@link Shell}.
     * 
     * 
     * @param errorListener
     *            The error listener to be removed.
     * 
     * @see #addErrorListener(ShellListener)
     */
    public void removeErrorListener(ShellListener errorListener) {
        this.errorListener = ShellListeners.remove(this.errorListener, errorListener);
    }

    /**
     * Removes an {@link ShellListener} from this {@link Shell}.
     * 
     * 
     * @param outputListener
     *            The output listener to be removed.
     * 
     * @see #addErrorListener(ShellListener)
     */
    public void removeOutputListener(ShellListener outputListener) {
        this.outputListener = ShellListeners.remove(this.outputListener, outputListener);
    }

    /**
     * Gets the underlying command as a {@link String}.
     * 
     * @return The underlying command as a {@link String}.
     */
    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        for (int i = 0, n = command.size(); i < n; i++) {
            if (i > 0) {
                text.append(" ");
            }
            text.append(command.get(i));
        }
        return text.toString();
    }

    private void readStream(InputStream stream, Supplier<ShellListener> listener) {
        try {
            byte[] buffer = new byte[8192];
            for (int read = -1; (read = stream.read(buffer)) != -1; listener.get().receive(new String(buffer, 0, read))) {}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}