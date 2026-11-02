package com.sistematurnos.agenda.application.port.in.command;

public record RegistrarClienteCommand(String nombre, String telefono, String correo) { }
