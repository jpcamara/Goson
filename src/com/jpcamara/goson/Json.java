package com.jpcamara.goson;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.math.*;

import java.util.Date;
import java.math.BigInteger;
import java.math.BigDecimal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gw.lang.reflect.IType;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.gs.IGosuObject;
import gw.lang.reflect.java.IJavaType;

public class Json implements IGosuObject {
	private JSONObject json;
	private IType type;
	
	public Json(IType type) {
	  this.type = type;
		this.json = new JSONObject();
	}
	
	public Json(Object json, IType type) {
		if ((json instanceof JSONObject) == false) {
			throw new JSONParserException("Must be a JSONObject");
		}
	  this.type = type;
		this.json = (JSONObject)json;
	}
	
	/**
	* Creates Json object, validating structure of the json 
	* string against the JsonTypeInfo provided.
	*/
	public Json(String json, JsonTypeInfo structure) {
		try {
		  this.type = structure.getOwnersType();
/*			this.json = new JSONObject(json);*/
			this.json = createJson(new JSONObject(json), structure);
		} catch (JSONException e) {
			throw new JSONParserException(e);
		}
	}
	
	//TODO add validity checks to verify the type is correct before parsing it.
  private JSONObject createJson(JSONObject it, JsonTypeInfo structure) throws JSONException {
    for (IPropertyInfo info : structure.getProperties()) {
      IType featureType = info.getFeatureType();
      String jsonName = structure.getJsonPropertyName(info.getName());
/*      System.out.println("jsonName: " + jsonName + " - feature type: " + featureType);*/
      if (!it.has(jsonName)) {
        continue;
      }
      Object o = it.get(jsonName);
      it.remove(jsonName);
      
      if (featureType == IJavaType.BOOLEAN) {
        it.put(jsonName, o);
      } else if (featureType == IJavaType.STRING) {
        it.put(jsonName, o);
      } else if (featureType == IJavaType.BIGDECIMAL) {
        it.put(jsonName, new BigDecimal(o instanceof Long ? Long.valueOf(o.toString()) : Double.valueOf(o.toString())));
      } else if (featureType == IJavaType.BIGINTEGER) {
        it.put(jsonName, BigInteger.valueOf(Long.valueOf(o.toString())));
      } else if (featureType == IJavaType.DATE) {
        it.put(jsonName, new Date((String)o)); //TODO obviously bad
      } else if (featureType instanceof gw.internal.gosu.parser.IJavaTypeInternal) { //hack to determine list
        System.out.println("parse: " + jsonName);
        JSONArray arr = (JSONArray)o;
        if (arr.length() == 0) {
          continue;
        }
				ArrayList rawList = new ArrayList();
				
				for (int i = 0; i < arr.length(); i++) {
				  IType parameterizedType = featureType.getTypeParameters()[0];
				  if (parameterizedType == IJavaType.BOOLEAN) {
            rawList.add(arr.get(i));
          } else if (parameterizedType == IJavaType.STRING) {
            System.out.println("in ijavatype string: " + arr.get(i));
            rawList.add(arr.get(i));
          } else if (parameterizedType == IJavaType.BIGDECIMAL) {
            rawList.add(new BigDecimal(o instanceof Long ? Long.valueOf(arr.get(i).toString()) :
              Double.valueOf(arr.get(i).toString())));
          } else if (parameterizedType == IJavaType.BIGINTEGER) {
            rawList.add(BigInteger.valueOf(Long.valueOf(arr.get(i).toString())));
          } else if (parameterizedType == IJavaType.DATE) {
            rawList.add(new Date((String)arr.get(i))); //TODO obviously bad
          } else if (parameterizedType instanceof IType) {
            rawList.add(new Json(createJson((JSONObject)arr.get(i), 
              (JsonTypeInfo)parameterizedType.getTypeInfo()), parameterizedType));
				  }
				}
				it.put(jsonName, (Object)rawList); //cast it so it doesn't get transformed to a JSONArray
      } else if (featureType instanceof IType) { //hack to determine jsontype
/*        System.out.println("in create jsontype: " + featureType);*/
        it.put(jsonName, new Json(createJson((JSONObject)o, (JsonTypeInfo)featureType.getTypeInfo()), featureType));
      }
    }
		return it;
	}
	
	public Json(Object json) {
		if ((json instanceof JSONObject) == false) {
			throw new JSONParserException("Must be a JSONObject");
		}
		this.json = (JSONObject)json;
	}
	
	public String serialize() {
		JSONObject output = serializeAsJSONObject();
		return output.toString();
	}
	
	private JSONObject serializeAsJSONObject() {
		JSONObject output = new JSONObject();

		try {
			for (String key : keys()) {
				JsonName name = new JsonName(key);
				Object value = get(key);
				
				if (value instanceof List) {
				  
				  
					List list = (List)value;
					JSONArray array = new JSONArray();
					if (output.has(name.getJsonName())) {
					  array = output.getJSONArray(name.getJsonName());
					} else {
					  output.put(name.getJsonName(), array);
					}
					
					if (list.size() > 0) {
/*            System.out.println("list of: " + list.get(0).getClass() + " - length: " + list.size());*/
					}
					if (list.size() > 0 && list.get(0) instanceof Json) {
/*            System.out.println("writing json");*/
						List<Json> jsons = (List<Json>)list;
						for (Json j : jsons) {
							array.put(j.serializeAsJSONObject());
/*              System.out.println("output right now:     " + output.toString());*/
						}
					} else {
/*            System.out.println("writing a list of objects");*/
						for (Object o : list) {
						  if (o instanceof Boolean || o instanceof String) {
						    array.put(o);
						  } else if (o instanceof java.math.BigDecimal) {
						    array.put(((BigDecimal)o).doubleValue()); //TODO loss of precision
						  } else if (o instanceof java.math.BigInteger) {
						    array.put(((BigInteger)o).longValue());
						  } else if (o instanceof java.util.Date) {
						    array.put(o.toString());
						  }
						}
					}
					
					
				} else if (value instanceof Json) {
				  
				  
					Json current = (Json)value;//get(name.getJsonName());
					output.put(name.getJsonName(), current.serializeAsJSONObject());
					
					
				} else if (value instanceof JSONArray) {
				  
				  
				  JSONArray arr = (JSONArray)value;
				  for (int i = 0; i < arr.length(); i++) {
				    Object o = arr.get(i);
				    if (o instanceof Json) {
				      arr.put(i, ((Json)o).serializeAsJSONObject());
				    } else if (o instanceof Boolean || o instanceof String) {
					    arr.put(i, o);
					  } else if (o instanceof java.math.BigDecimal) {
					    arr.put(i, ((BigDecimal)o).doubleValue()); //TODO loss of precision
					  } else if (o instanceof java.math.BigInteger) {
					    arr.put(i, ((BigInteger)o).longValue());
					  } else if (o instanceof java.util.Date) {
					    arr.put(i, o.toString());
					  }
				  }
				  
				  
				} else if (value instanceof Boolean || value instanceof String) {
			    output.put(name.getJsonName(), value);
			  } else if (value instanceof java.math.BigDecimal) {
			    output.put(name.getJsonName(), ((BigDecimal)value).doubleValue());
			  } else if (value instanceof java.math.BigInteger) {
			    output.put(name.getJsonName(), ((BigInteger)value).longValue());
			  } else if (value instanceof java.util.Date) {
			    output.put(name.getJsonName(), value.toString());
			  }
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		
/*    System.out.println("End of writing: " + output.toString());*/
		return output;
	}
	
	public Object get(String key) {
		try {
			if (json.has(key) == false) {
				return null;
			}
			return json.get(key);
		} catch (JSONException e) {
			throw new JSONParserException(e);		
		}
	}
	
	public Object getWithIndex(String key, int index) {
		try {
			if (json.has(key) == false) {
				return null;
			}
			Object o = json.get(key);
			if (JsonParser.isJSONArray(o) == false) {
				throw new JSONParserException(key + " is not an array");
			}
			return ((JSONArray)o).get(index);
		} catch (JSONException e) {
			throw new JSONParserException(e);
		}
	}
	
	public void put(String key, Object value) {
		try {
			json.put(key, value);
/*      System.out.println(json.get(key).getClass());*/
		} catch (JSONException e) {
			throw new JSONParserException(e);
		}
	}
	
	public Json getJson(String key) {
		try {
			return new Json(json.getJSONObject(key), type);
		} catch (JSONException e) {
			throw new JSONParserException(e);
		}
	}
	
	public String toString() {
		String str = "[";
		for (String key : keys()) {
			str += key + ",";
		}
		str += "]";
		return str;
	}
	
	public Iterable<String> keys() {
		return new IterableJson();
	}
	
	@Override
	public IType getIntrinsicType() {
	  return type;
	}
	
	private class IterableJson implements Iterable<String> {
		@SuppressWarnings("unchecked")
		@Override
		public Iterator<String> iterator() {
			return (Iterator<String>)json.keys();
		}
	}
}
