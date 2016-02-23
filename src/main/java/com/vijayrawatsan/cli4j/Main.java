package com.vijayrawatsan.cli4j;

import com.google.common.util.concurrent.FutureCallback;

/**
 * Created by vijayrawatsan on 24/02/16.
 */
public class Main {
    public static void main(String[] args) {
        Commander.builder()
                .command("tesseract a-0.png out")
                .directory("/Users/vijayrawatsan/ocr-demo")
                .onComplete(new FutureCallback<CommandResult>() {
                    public void onSuccess(CommandResult commandResult) {
                        System.out.println(commandResult.toString());
                    }

                    public void onFailure(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }).execute();
    }
}
