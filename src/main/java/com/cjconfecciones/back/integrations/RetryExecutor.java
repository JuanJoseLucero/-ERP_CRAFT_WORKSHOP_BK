package com.cjconfecciones.back.integrations;

import jakarta.resource.spi.RetryableException;

public class RetryExecutor {

    public static void execute(int maxRetries, long delayMs, RetryableOperation operation) throws Exception{
         int attempt = 0;
         while(true){
             try{
                 operation.execute();
                 return;
             }catch (Exception e){
                 attempt++;
                 if(attempt >= maxRetries){
                     throw new RuntimeException("Falló despues de ".concat(String.valueOf(attempt)).concat(" intentos"),e);
                 }
                 System.out.println("Reintentando intento ".concat(String.valueOf(attempt)));
                 Thread.sleep(delayMs);
             }
         }
    }

    @FunctionalInterface
    public interface RetryableOperation{
        void execute() throws Exception;
    }
}
