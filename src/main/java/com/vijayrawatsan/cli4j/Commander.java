package com.vijayrawatsan.cli4j;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Created by vijayrawatsan on 24/02/16.
 */
public class Commander {

    private final static Logger LOGGER = Logger.getLogger(Commander.class.getName());

    private static ListeningExecutorService pool = null;
    private static List<WeakReference<ExecutorService>> passedExecutors = new ArrayList<WeakReference<ExecutorService>>();
    private final String[] command;
    private final String directory;
    private final FutureCallback<CommandResult> futureCallback;
    private final ExecutorService executorService;

    public Commander(String[] command, String directory, FutureCallback<CommandResult> futureCallback, ExecutorService executorService) {
        this.command = command;
        this.directory = directory;
        this.futureCallback = futureCallback;
        this.executorService = executorService;
    }

    public static CommandBuilder builder() {
        return new CommandBuilder();
    }

    public static void shutDown() {
        shutDown(false);
    }

    public static void shutDown(boolean shutDownPassedExecutors) {
        if (pool != null) {
            pool.shutdown();
        }
        if (shutDownPassedExecutors) {
            for (WeakReference<ExecutorService> serviceWeakReference : passedExecutors) {
                ExecutorService executorService = serviceWeakReference.get();
                if (executorService != null) {
                    executorService.shutdown();
                }
            }
        }
    }

    private void execute() {
        ListeningExecutorService pool = null;
        if (executorService == null) {
            pool = getPool();
        } else {
            passedExecutors.add(new WeakReference<ExecutorService>(executorService));
            pool = MoreExecutors.listeningDecorator(executorService);
        }
        ListenableFuture<CommandResult> future = pool.submit(new Callable<CommandResult>() {
            public CommandResult call() throws Exception {
                return executeSync(command, directory);
            }
        });
        if (futureCallback != null) {
            Futures.addCallback(future, futureCallback);
        }
    }

    private synchronized static ListeningExecutorService getPool() {
        if (pool == null) {
            pool = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
        }
        return pool;
    }

    public static class CommandBuilder {
        private String[] command;
        private String directory;
        private FutureCallback<CommandResult> futureCallback;
        private ExecutorService executorService;

        private CommandBuilder() {
        }

        public CommandBuilder command(String command) {
            Preconditions.checkNotNull(command);
            return this.command(command.trim().replaceAll(" +", " ").split(" "));
        }

        public CommandBuilder command(String[] command) {
            Preconditions.checkNotNull(command);
            this.command = command;
            return this;
        }

        public CommandBuilder directory(String directory) {
            Preconditions.checkNotNull(directory);
            this.directory = directory;
            return this;
        }

        public CommandBuilder executor(ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        public CommandBuilder onComplete(FutureCallback<CommandResult> futureCallback) {
            this.futureCallback = futureCallback;
            return this;
        }

        public void execute() {
            Preconditions.checkNotNull(command);
            Preconditions.checkNotNull(directory);
            Commander commander = new Commander(command, directory, futureCallback, executorService);
            commander.execute();
        }
    }

    public static CommandResult executeSync(String command, String directory) {
        Preconditions.checkNotNull(command);
        Preconditions.checkNotNull(directory);
        return executeSync(command.trim().replaceAll(" +", " ").split(" "), directory);
    }

    public static CommandResult executeSync(String[] command, String directory) {
        Preconditions.checkNotNull(command);
        Preconditions.checkNotNull(directory);

        LOGGER.info("Running command : " + Arrays.asList(command));
        StringBuilder out = new StringBuilder();
        int status = 0;
        try {
            //Setup process builder
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(directory));
            pb.redirectErrorStream(true);

            //Start process
            Process process = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line, previous = null;
            while ((line = br.readLine()) != null) {
                if (!line.equals(previous)) {
                    previous = line;
                    out.append(line).append('\n');
                }
            }
            status = process.waitFor();

            //Check status
            if (status == 0) {
                return new CommandResult(status, out.toString(), null);
            } else {
                return new CommandResult(status, null, out.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommandResult(status, null, e.getMessage() + "|" + out.toString());
        }
    }
}
