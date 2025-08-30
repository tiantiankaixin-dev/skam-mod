package com.example.skam.util;

public interface IAirJumperState {
    int getAirJumpsUsed();
    void setAirJumpsUsed(int jumps);
    void setTicksInAir(int ticks);
}
