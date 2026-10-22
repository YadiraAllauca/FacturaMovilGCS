package dev.android.appfacturador.database

import dev.android.appfacturador.model.CLIENTE
import dev.android.appfacturador.model.EMPLEADO
import dev.android.appfacturador.model.FACTURA
import dev.android.appfacturador.model.PRODUCTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ClientDao {
    @GET("Cliente.json")
    fun getClients(): Call<List<CLIENTE>>
    @POST("Cliente.json")
    fun addClient(@Body membership: CLIENTE): Call<CLIENTE>
    @PUT("Cliente/{id}.json")
    fun updateClient(@Path("id") id:String, @Body membership: CLIENTE): Call<CLIENTE>
}

interface ProductDao {
    @GET("Product.json")
    fun getProducts(): Call<List<PRODUCTO>>
    @POST("Product.json")
    fun addProduct(@Body membership: PRODUCTO): Call<PRODUCTO>
    @DELETE("Product/{id}.json")
    fun deleteProduct(@Path("id") id:String): Call<PRODUCTO>
    @PUT("Product/{id}.json")
    fun updateProduct(@Path("id") id:String, @Body membership: PRODUCTO): Call<PRODUCTO>
}
interface EmployeeDao {
    @POST("Empleado.json")
    fun addEmployee(@Body membership: EMPLEADO): Call<EMPLEADO>
    @DELETE("Empleado/{id}.json")
    fun deleteEmployee(@Path("id") id:String): Call<EMPLEADO>
    @PUT("Empleado/{id}.json")
    fun updateEmployee(@Path("id") id:String, @Body membership: EMPLEADO): Call<EMPLEADO>
}

interface BillDao {
    @POST("Factura.json")
    fun addBill(@Body membership: FACTURA): Call<FACTURA>
    @PUT("Factura/{id}.json")
    fun updateBill(@Path("id") id:String, @Body membership: FACTURA): Call<FACTURA>
}

