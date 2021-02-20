package cat.mrtxema.covid.datasource.aemet;

class OperationResponse {
    private String descripcion;
    private int estado;
    private String datos;
    private String metadatos;

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public String getDatos() {
        return datos;
    }

    public void setDatos(String datos) {
        this.datos = datos;
    }

    public String getMetadatos() {
        return metadatos;
    }

    public void setMetadatos(String metadatos) {
        this.metadatos = metadatos;
    }
}
