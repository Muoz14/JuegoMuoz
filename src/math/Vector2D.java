package math;

// Parte matematica del juego
public class Vector2D {

    private double x, y;

    // 2 constructores utilizando sobrecarga de metodos
    // para pedir e iniciarlizar las variables (x,y) que son para posiciones relativas

    public Vector2D(double x, double y){

        this.x = x;
        this.y = y;

    }

    public Vector2D(){

        x = 0;
        y = 0;

    }


    //Vector para velocidad
    public Vector2D add(Vector2D v){

        return new Vector2D(x + v.getX(), y + v.getY());

    }

    //Resta de vectores
    public Vector2D subtract(Vector2D v){

        return new Vector2D(x - v.getX(), y - v.getY());

    }


    //limitar aceleracion o desaceleracion
    public Vector2D limit(double value){

        if (getMagnitude() > value){

            return this.normalize().scale(value);

        }

        return this;

    }

    //Para normalizar vector y obtener magnitud = 1
    public Vector2D normalize(){

        double magnitude = getMagnitude();

        return new Vector2D(x / magnitude, y / magnitude);

    }

    //Para modificar la magnitud del vector 'heading' o direccion del jugador
    public Vector2D scale(double value){

        return new Vector2D(x * value, y * value);

    }

    // Dentro de Vector2D
    public Vector2D rotate(double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double newX = x * cos - y * sin;
        double newY = x * sin + y * cos;
        return new Vector2D(newX, newY);
    }

    //Obtener magnitud
    public double getMagnitude(){

        return Math.sqrt(x*x + y*y);

    }

    //obtener direccion
    public Vector2D setDirection(double angle){

        double magnitude = getMagnitude();

        return new Vector2D(Math.cos(angle) * magnitude, Math.sin(angle) * magnitude);

    }

    public double getAngle(){

        return Math.asin(y/getMagnitude());

    }

    //Geter and seter generados
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

}
