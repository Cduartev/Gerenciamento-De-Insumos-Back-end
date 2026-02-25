package com.projeto.gerenciamento.de.insumos.service;

import com.projeto.gerenciamento.de.insumos.domain.entity.ItemComposicaoProduto;
import com.projeto.gerenciamento.de.insumos.domain.entity.MateriaPrima;
import com.projeto.gerenciamento.de.insumos.domain.entity.Produto;
import com.projeto.gerenciamento.de.insumos.domain.repository.MateriaPrimaRepository;
import com.projeto.gerenciamento.de.insumos.domain.repository.ProdutoRepository;
import com.projeto.gerenciamento.de.insumos.dto.produto.AtualizarProdutoRequest;
import com.projeto.gerenciamento.de.insumos.dto.produto.CriarProdutoRequest;
import com.projeto.gerenciamento.de.insumos.dto.produto.ItemComposicaoProdutoRequest;
import com.projeto.gerenciamento.de.insumos.dto.produto.ProdutoResponse;
import com.projeto.gerenciamento.de.insumos.exception.RecursoNaoEncontradoException;
import com.projeto.gerenciamento.de.insumos.exception.RegraDeNegocioException;
import com.projeto.gerenciamento.de.insumos.mapper.ProdutoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final MateriaPrimaRepository materiaPrimaRepository;
    private final ProdutoMapper produtoMapper;

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarTodos() {
        return produtoRepository.findAll()
                .stream()
                .map(produtoMapper::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Long id) {
        Produto produto = buscarEntidadeComComposicaoPorId(id);
        return produtoMapper.paraResponse(produto);
    }

    @Transactional
    public ProdutoResponse criar(CriarProdutoRequest request) {
        validarCodigoDuplicadoNaCriacao(request.codigo());
        validarItensComposicao(request.itensComposicao());

        Produto produto = Produto.builder()
                .codigo(request.codigo())
                .nome(request.nome())
                .valor(request.valor())
                .build();

        adicionarItensComposicao(produto, request.itensComposicao());

        Produto salvo = produtoRepository.save(produto);
        Produto produtoCompleto = buscarEntidadeComComposicaoPorId(salvo.getId());

        return produtoMapper.paraResponse(produtoCompleto);
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, AtualizarProdutoRequest request) {
        Produto produto = buscarEntidadeComComposicaoPorId(id);

        validarCodigoDuplicadoNaAtualizacao(request.codigo(), id);
        validarItensComposicao(request.itensComposicao());

        produto.setCodigo(request.codigo());
        produto.setNome(request.nome());
        produto.setValor(request.valor());

        produto.limparItensComposicao();
        produtoRepository.saveAndFlush(produto); // garante remoção no banco antes de re-adicionar

        adicionarItensComposicao(produto, request.itensComposicao());

        Produto atualizado = produtoRepository.save(produto);
        Produto produtoCompleto = buscarEntidadeComComposicaoPorId(atualizado.getId());

        return produtoMapper.paraResponse(produtoCompleto);
    }

    @Transactional
    public void remover(Long id) {
        Produto produto = buscarEntidadePorId(id);
        produtoRepository.delete(produto);
    }

    private Produto buscarEntidadePorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto nao encontrado para o id: " + id));
    }

    private Produto buscarEntidadeComComposicaoPorId(Long id) {
        return produtoRepository.buscarPorIdComComposicao(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto nao encontrado para o id: " + id));
    }

    private void validarCodigoDuplicadoNaCriacao(String codigo) {
        if (produtoRepository.existsByCodigo(codigo)) {
            throw new RegraDeNegocioException("Ja existe produto com o codigo informado.");
        }
    }

    private void validarCodigoDuplicadoNaAtualizacao(String codigo, Long id) {
        if (produtoRepository.existsByCodigoAndIdNot(codigo, id)) {
            throw new RegraDeNegocioException("Ja existe outro produto com o codigo informado.");
        }
    }

    private void validarItensComposicao(List<ItemComposicaoProdutoRequest> itensComposicao) {
        Set<Long> idsMateriasPrimas = new HashSet<>();

        for (ItemComposicaoProdutoRequest item : itensComposicao) {
            if (!idsMateriasPrimas.add(item.materiaPrimaId())) {
                throw new RegraDeNegocioException(
                        "A composicao nao pode conter materia-prima repetida. Id repetido: " + item.materiaPrimaId()
                );
            }
        }
    }

    private void adicionarItensComposicao(Produto produto, List<ItemComposicaoProdutoRequest> itensRequest) {
        List<Long> idsMateriasPrimas = itensRequest.stream()
                .map(ItemComposicaoProdutoRequest::materiaPrimaId)
                .toList();

        List<MateriaPrima> materiasPrimas = materiaPrimaRepository.findAllById(idsMateriasPrimas);

        if (materiasPrimas.size() != idsMateriasPrimas.size()) {
            Set<Long> idsEncontrados = materiasPrimas.stream()
                    .map(MateriaPrima::getId)
                    .collect(Collectors.toSet());

            List<Long> idsNaoEncontrados = idsMateriasPrimas.stream()
                    .filter(id -> !idsEncontrados.contains(id))
                    .distinct()
                    .toList();

            throw new RecursoNaoEncontradoException(
                    "Materia-prima(s) nao encontrada(s) para os ids: " + idsNaoEncontrados
            );
        }

        Map<Long, MateriaPrima> materiasPrimasPorId = materiasPrimas.stream()
                .collect(Collectors.toMap(MateriaPrima::getId, Function.identity()));

        for (ItemComposicaoProdutoRequest itemRequest : itensRequest) {
            MateriaPrima materiaPrima = materiasPrimasPorId.get(itemRequest.materiaPrimaId());

            ItemComposicaoProduto item = ItemComposicaoProduto.builder()
                    .materiaPrima(materiaPrima)
                    .quantidadeNecessaria(itemRequest.quantidadeNecessaria())
                    .build();

            produto.adicionarItemComposicao(item);
        }
    }
}
