package common;

public class ServerData {



    public Integer server_id;
    public String ip;
    public Integer port;


    public ServerData(Integer id, String ip, Integer port){
        this.server_id = id;
        this.ip = ip;
        this.port = port;
    }

    public ServerData() { // Seems necessary

    }

    public Integer getId(){
        return this.server_id;
    }

    public String getIp(){
        return this.ip;
    }

    public Integer getPort(){
        return this.port;
    }


    public void setId(Integer id){
        this.server_id = id;
    }

    public void setIp(String ip){
        this.ip = ip;
    }


    public void setPort(Integer port){
        this.port = port;
    }


    public String getJson(){
        return "{\"server_id\":\""+this.getId()+"\",\"ip\":\""+this.getIp()+"\", \"port\":\""+this.getPort()+"\"}";
    }


}
