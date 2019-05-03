package com.example.myapp1.application.commons;

public class Commons {
    private StringBuilder buffer;

    public Commons() {
        this.buffer = new StringBuilder(""); //początkowa wartość w buforze
    }

    public void addDataToBuffer(String data){
        synchronized (buffer) {
            buffer.append(data);
        }
    }

    public String readAllFromBuffer(){
        String result="";
        synchronized (buffer){
            if(buffer.length()>0) {
                result = buffer.toString();
                buffer.delete(0, buffer.length());
            }
        }
        return result;
    }
}
