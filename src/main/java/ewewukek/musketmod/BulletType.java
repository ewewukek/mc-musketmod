package ewewukek.musketmod;

public enum BulletType {
    BULLET,
    PELLET;

    public byte toByte() {
        switch(this) {
        case BULLET:
            return 0;
        case PELLET:
            return 1;
        default:
            return 0;
        }
    }

    public static BulletType fromByte(byte b) {
        switch(b) {
        case 0:
            return BULLET;
        case 1:
            return PELLET;
        default:
            return null;
        }
    }
}
