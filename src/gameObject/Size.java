package gameObject;
import graphics.Assets;
import java.awt.image.BufferedImage;

public enum Size {


    BIG(2, "MED"),
    MED(2, "SMALL"),
    SMALL(2, "TINY"),
    TINY(0, null);

    public int quantity;

    private String next; // el nombre del siguiente tipo de meteorito

    private Size(int quantity, String next){

        this.quantity = quantity;
        this.next = next;

    }

    // Metodo para obtener el siguiente tamaño
    public Size getNextSize(){

        if(next == null) return null; // ya no hay más después
        return Size.valueOf(next); // convierte el texto en el siguiente enum

    }

    // Obtener textura según familia
    public BufferedImage getTexture(int family){
        switch (this) {
            case BIG:
                return Assets.bigs[family];
            case MED:
                return Assets.meds[family];
            case SMALL:
                return Assets.smalls[family];
            case TINY:
                return Assets.tinies[family];
            default:
                return null;
        }
    }

}
