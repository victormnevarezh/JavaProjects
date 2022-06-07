package web;

import datos.ClienteDaoJDBC;
import datos.CompraDaoJDBC;
import dominio.Cliente;
import dominio.Compra;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/ServletControlador")
public class ServletControlador extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String accion = request.getParameter("accion");
        if (accion != null) {
            switch (accion) {
                case "editar":
                    this.editarCliente(request, response);
                    break;
                case "editarCompra":
                    this.editarCompra(request, response);
                    break;
                case "eliminar":
                    this.eliminarCliente(request, response);
                    break;
                case "compras":
                    this.getComprasCliente(request, response);
                    break;
                case "comprasGeneral":
                    this.listadoComprasGeneral(request, response);
                    break;
                case "eliminarCompra":
                    this.eliminarCompra(request, response);
                    break;
                default:
                    this.accionDefault(request, response);
            }
        } else {
            this.accionDefault(request, response);
        }
    }

    private void accionDefault(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Cliente> clientes = new ClienteDaoJDBC().listar();
        for (Cliente cliente : clientes){
            this.calcularSaldoCliente(cliente);
        }
        System.out.println("clientes = " + clientes);
        HttpSession sesion = request.getSession();
        sesion.setAttribute("clientes", clientes);
        sesion.setAttribute("totalClientes", clientes.size());
        sesion.setAttribute("saldoTotal", this.calcularSaldoTotal(clientes));
        sesion.setAttribute("clienteMax", this.maximoDeuda(clientes));
        sesion.setAttribute("clienteMin", this.minimoDeuda(clientes));
        request.getRequestDispatcher("clientes.jsp").forward(request, response);
        response.sendRedirect("clientes.jsp");
    }
    
    private void listadoComprasGeneral(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Compra> compras = new CompraDaoJDBC().listarCompras();
        
            System.out.println("compras = " + compras);
            HttpSession sesion = request.getSession();
            sesion.setAttribute("compras", compras);
            sesion.setAttribute("totalCompras", compras.size());
            sesion.setAttribute("montoTotal", this.calcularMontoTotal(compras));
            request.getRequestDispatcher("compras.jsp").forward(request, response);
            response.sendRedirect("compras.jsp");
        
        
    }
    
    private double calcularMontoTotal(List<Compra> compras) {
        double montoTotal = 0;
        for (Compra compra : compras) {
            montoTotal += compra.getMonto();
        }
        return montoTotal;
    }

    private double calcularSaldoTotal(List<Cliente> clientes) {
        double saldoTotal = 0;
        for (Cliente cliente : clientes) {
            saldoTotal += cliente.getSaldo();
        }
        return saldoTotal;
    }
    
    private void calcularSaldoCliente(Cliente cliente){
        double saldoTotal = 0;
        List<Compra> compras = new CompraDaoJDBC().listarComprasPorId(cliente);
        for(Compra compra: compras){
            if(cliente.getIdCliente() == compra.getId_cliente()){
                saldoTotal+=compra.getMonto();
            }
        }
        cliente.setSaldo(saldoTotal);
        int registrosModificados = new ClienteDaoJDBC().actualizar(cliente);
        System.out.println("registrosModificados = " + registrosModificados);
    }

    private void editarCliente(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //recuperamos el idCliente
        int idCliente = Integer.parseInt(request.getParameter("idCliente"));
        Cliente cliente = new ClienteDaoJDBC().encontrar(new Cliente(idCliente));
        request.setAttribute("cliente", cliente);
        String jspEditar = "/WEB-INF/paginas/cliente/editarCliente.jsp";
        // se crea ruta para navegar y que despecha el servlet
        request.getRequestDispatcher(jspEditar).forward(request, response);
    }
    
    private void editarCompra(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //recuperamos el idCompra
        int id_compra = Integer.parseInt(request.getParameter("idCompra"));
        Compra compra = new CompraDaoJDBC().encontrarCompra(new Compra(id_compra));
        request.setAttribute("compra", compra);
        String jspEditar = "/WEB-INF/paginas/compra/editarCompra.jsp";
        // se crea ruta para navegar y que despecha el servlet
        request.getRequestDispatcher(jspEditar).forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if (accion != null) {
            switch (accion) {
                case "insertar":
                    this.insertarCliente(request, response);
                    break;
                case "modificar":
                    this.modificarCliente(request, response);
                    break;
                case "insertarCompra":
                    this.insertarCompra(request, response);
                    break;
                case "modificarCompra":
                    this.modificarCompra(request, response);
                    break;
                default:
                    this.accionDefault(request, response);
            }
        } else {
            this.accionDefault(request, response);
        }
    }

    private void insertarCliente(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //recuperamos los valores del formulario agregarCliente
        String nombre = request.getParameter("nombre");
        String apellido = request.getParameter("apellido");
        String email = request.getParameter("email");
        String telefono = request.getParameter("telefono");
        double saldo = 0;
        String saldoString = request.getParameter("saldo");
        if (saldoString != null && !"".equals(saldoString)) {
            saldo = Double.parseDouble(saldoString);
        }

        //Creamos el objeto de cliente (modelo)
        Cliente cliente = new Cliente(nombre, apellido, email, telefono, saldo);

        //Insertamos el nuevo objeto en la base de datos
        int registrosModificados = new ClienteDaoJDBC().insertar(cliente);
        System.out.println("registrosModificados = " + registrosModificados);

        //Redirigimos hacia accion por default
        this.accionDefault(request, response);
    }

    private void modificarCliente(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //recuperamos los valores del formulario editarCliente
        int idCliente = Integer.parseInt(request.getParameter("idCliente"));
        String nombre = request.getParameter("nombre");
        String apellido = request.getParameter("apellido");
        String email = request.getParameter("email");
        String telefono = request.getParameter("telefono");
        double saldo = 0;
        String saldoString = request.getParameter("saldo");
        if (saldoString != null && !"".equals(saldoString)) {
            saldo = Double.parseDouble(saldoString);
        }

        //Creamos el objeto de cliente (modelo)
        Cliente cliente = new Cliente(idCliente, nombre, apellido, email, telefono, saldo);

        //Modificar el  objeto en la base de datos
        int registrosModificados = new ClienteDaoJDBC().actualizar(cliente);
        System.out.println("registrosModificados = " + registrosModificados);

        //Redirigimos hacia accion por default
        this.accionDefault(request, response);
    }
    
    private void modificarCompra(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //recuperamos los valores del formulario editarCompra
        int id_compra = Integer.parseInt(request.getParameter("id_compra"));
        double monto = 0;
        String montoString = request.getParameter("monto");
        if (montoString != null && !"".equals(montoString)) {
            monto = Double.parseDouble(montoString);
        }

        //Creamos el objeto de cliente (modelo)
        Compra compra = new Compra(id_compra, monto, false);

        //Modificar el  objeto en la base de datos
        int registrosModificados = new CompraDaoJDBC().actualizarCompra(compra);
        System.out.println("registrosModificados = " + registrosModificados);

        //Redirigimos hacia accion por default
        this.accionDefault(request, response);
    }
    
    private void eliminarCliente(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        //recuperamos los valores del formulario editarCliente
        int idCliente = Integer.parseInt(request.getParameter("idCliente"));
     

        //Creamos el objeto de cliente (modelo)
        Cliente cliente = new Cliente(idCliente);

        //Eliminamos el  objeto en la base de datos
        int registrosModificados = new ClienteDaoJDBC().eliminar(cliente);
        System.out.println("registrosModificados = " + registrosModificados);

        //Redirigimos hacia accion por default
        this.accionDefault(request, response);
    }
        
    private void getComprasCliente(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        int idCliente = Integer.parseInt(request.getParameter("idCliente"));
        List<Compra> compras = new CompraDaoJDBC().listarComprasPorId(new Cliente(idCliente));
        System.out.println("compras = " + compras);
        HttpSession sesion = request.getSession();
        sesion.setAttribute("compras", compras);
        sesion.setAttribute("totalCompras", compras.size());
        sesion.setAttribute("montoTotal", this.calcularMontoTotal(compras));
        request.getRequestDispatcher("compras.jsp").forward(request, response);
        response.sendRedirect("compras.jsp");
    }

    private void eliminarCompra(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        int id_compra = Integer.parseInt(request.getParameter("id_compra"));
        Compra compra = new Compra(id_compra);
        int registrosModificados = new CompraDaoJDBC().eliminarCompra(compra);
        System.out.println("registrosModificados = " + registrosModificados);
        this.accionDefault(request, response);
    }
    
    private void insertarCompra(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int idCliente = Integer.parseInt(request.getParameter("idCliente"));
 
        double monto = 0;
        String montostr = request.getParameter("monto");
        if (montostr != null && !"".equals(montostr)) {
            monto = Double.parseDouble(montostr);
        }
        Compra compra = new Compra(idCliente, monto);
        int registrosModificados = new CompraDaoJDBC().insertarCompra(compra);
        System.out.println("registrosAgregados = " + registrosModificados);
        this.accionDefault(request, response);
    }
    
    private String maximoDeuda(List<Cliente> clientes){
        Double saldo = 0.0;
        Cliente clienteAux = new Cliente();
        for(Cliente cliente:clientes){
            if(cliente.getSaldo()>saldo){
                saldo = cliente.getSaldo();
                clienteAux=cliente;
            }
        }
         String nombre = clienteAux.getNombre()+ " " + clienteAux.getApellido();
        
        if(clienteAux.getNombre()==""|| clienteAux.getNombre()==null){
            nombre = "Los clientes no tienen deudas";
        }
        
        return nombre;
    }
    
    private String minimoDeuda(List<Cliente> clientes){
        Double saldo = clientes.get(0).getSaldo();
        Cliente clienteAux = clientes.get(0);
        for(Cliente cliente:clientes){
            if(cliente.getSaldo()<saldo){
                saldo=cliente.getSaldo();
                clienteAux = cliente;
            }
        }
        String nombre = clienteAux.getNombre()+ " " + clienteAux.getApellido();
        
        if(clienteAux.getNombre()=="" || clienteAux.getNombre()==null){
            nombre = "Los clientes no tienen deudas";
        }
        
        return nombre;
    }
}
