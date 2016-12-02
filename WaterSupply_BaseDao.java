package com.sucsoft.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unchecked")
public class BaseDao {

	@Autowired
	@Qualifier(value="sessionFactory")
	private SessionFactory sessionFactory;
	
	public <T> T get(Class<T> clasz,Serializable id){
		return getCurrentSession().get(clasz, id);
	}
	
	public void delete(Object obj){
		getCurrentSession().delete(obj);
	}
	
	public void update(Object obj){
		getCurrentSession().update(obj);
	}
	
	public Serializable save(Object obj){
		Serializable s = getCurrentSession().save(obj);
		return s;
	}
	
	public Long countSql(String sql,Object... params){
		return (Long)createSQLQuery(sql,params).getSingleResult();
	}
	
	public Long countSql(String sql,Map<String,Object> params){
		return (Long)createSQLQuery(sql,params).getSingleResult();
	}
	
	public <R> Query<R> createSQLQuery(String sql,Object... params){
		NativeQuery<R> q = (NativeQuery<R>)getCurrentSession().createNativeQuery(sql);
		if( params != null ){
			for(int i = 0,len = params.length ; i < len ;i ++ ){
				q.setParameter(i, params[i]);
			}
		}
		return q;
	}
	
	public <R> List<R> createPageSQLQuery(String sql,Integer start,Integer pageSize,Object... params){
		Query<R> q = createSQLQuery(sql,params);
		q.setFirstResult(start);
		q.setMaxResults(pageSize);
		return q.getResultList();
	}
	
	public <R> Query<R> createSQLQuery(String sql,Map<String,Object> params){
		Query<R> q = (Query<R>)getCurrentSession().createNativeQuery(sql);
		if( params != null ){
			params.forEach((k,v) -> {
				q.setParameter(k, v);
			}); 
		}
		return q;
	}
	
	public <T> List<T> createPageSQLQuery(String sql,Integer start,Integer pageSize,Map<String,Object> params){
		Query<T> q = createSQLQuery(sql,params);
		q.setFirstResult(start);
		q.setMaxResults(pageSize);
		return q.getResultList();
	}
	
	public Long count(String hql,Object... params){
		return (Long)createQuery(hql,params).getSingleResult();
	}
	public Long count(String hql,Map<String,Object> params){
		return (Long)createQuery(hql,params).getSingleResult();
	}
	
	public <T> Query<T> createQuery(String hql,Object... params){
		Query<T> q = (Query<T>)getCurrentSession().createQuery(hql);
		if( params != null ){
			for(int i = 0,len = params.length ; i < len ;i ++ ){
				q.setParameter(i, params[i]);
			}
		}
		return q;
	}
	
	public <T> List<T> createPageQuery(String hql,Integer start,Integer pageSize,Object... params){
		Query<T> q = createQuery(hql,params);
		q.setFirstResult(start);
		q.setMaxResults(pageSize);
		return q.getResultList();
	}
	
	public <T> Query<T> createQuery(String hql,Map<String,Object> params){
		Query<T> q = (Query<T>)getCurrentSession().createQuery(hql);
		if( params != null ){
			params.forEach((k,v) -> {
				q.setParameter(k, v);
			}); 
		}
		return q;
	}
	
	public <T> List<T> createPageQuery(String hql,Integer start,Integer pageSize,Map<String,Object> params){
		Query<T> q = createQuery(hql,params);
		q.setFirstResult(start);
		q.setMaxResults(pageSize);
		return q.getResultList();
	}
	
	private Session getCurrentSession(){
		return sessionFactory.getCurrentSession();
	}
}
