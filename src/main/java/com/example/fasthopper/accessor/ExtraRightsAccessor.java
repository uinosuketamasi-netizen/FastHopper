package com.example.fasthopper.accessor;

public interface ExtraRightsAccessor
{
    int fasthopper$getExtraRights();

    void fasthopper$consumeRight();

    boolean fasthopper$isInsideExtraTransfer();

    void fasthopper$setInsideExtraTransfer(
            boolean value
    );
}