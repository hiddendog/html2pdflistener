package br.com.christ.cdi;

public class KillEvent {
private Class beanType;
public KillEvent(Class beanType) {
this.beanType = beanType;
}
public Class getBeanType() {
return beanType;
}
}