package com.projetospring.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.projetospring.model.Pessoa;
import com.projetospring.model.Telefone;
import com.projetospring.repository.PessoaRepository;
import com.projetospring.repository.ProfissaoRepository;
import com.projetospring.repository.TelefoneRepository;

@Controller
public class PessoaController implements Serializable {
	private static final long serialVersionUID = 1L;

	@Autowired
	private PessoaRepository pessoaRepository;

	@Autowired
	private TelefoneRepository telefoneRepository;

	@Autowired
	private ReportUtil reportUtil;

	@Autowired
	private ProfissaoRepository profissaoRepository;

	@GetMapping(value = "/")
	public ModelAndView init() {
		ModelAndView modelAndView = new ModelAndView("/login");

		return modelAndView;
	}

	@GetMapping(value = "/cadastropessoa")
	public ModelAndView inicio() {
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa"); // serve pra retornar pra mesma
																					// tela // tela
		modelAndView.addObject("pessoaobj", new Pessoa());
		modelAndView.addObject("pessoa", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		modelAndView.addObject("profissoes", profissaoRepository.findAll());

		return modelAndView;
	
	}
	
	@PostMapping(value = "**/salvarpessoa", consumes = { "multipart/form-data" })
	public ModelAndView salvar(@Valid Pessoa pessoa, BindingResult bindingResult, final MultipartFile file)
			throws IOException {

		pessoa.setTelefones(telefoneRepository.getTelefone(pessoa.getId()));

		if (bindingResult.hasErrors()) {
			ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
			andView.addObject("pessoa", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
			andView.addObject("pessoaobj", pessoa);
			andView.addObject("profissoes", profissaoRepository.findAll());

			List<String> msg = new ArrayList<String>();
			for (ObjectError objectError : bindingResult.getAllErrors()) {
				msg.add(objectError.getDefaultMessage()); // vem das anotações do @NotEmpty e outras da entidade
			}
			andView.addObject("msg", msg);
			return andView;
		}

		if (file.getSize() > 0) { // Cadastrando
			pessoa.setCurriculo(file.getBytes());
			pessoa.setTipoFileCurriculo(file.getContentType());
			pessoa.setNomeFileCurriculo(file.getOriginalFilename());

		} else if (pessoa.getId() != null && pessoa.getId() > 0) { // Editando
			Pessoa pessoaTemp = pessoaRepository.findById(pessoa.getId()).get();
			pessoa.setCurriculo(pessoaTemp.getCurriculo());
			pessoa.setTipoFileCurriculo(pessoaTemp.getNomeFileCurriculo());
			pessoa.setNomeFileCurriculo(pessoaTemp.getTipoFileCurriculo());
		}

		pessoaRepository.save(pessoa);

		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoa", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		andView.addObject("profissoes", profissaoRepository.findAll());
		andView.addObject("pessoaobj", new Pessoa()); // objeto vazio

		return andView;
	}

	@GetMapping("/listapessoas")
	public ModelAndView pessoas() {
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoa", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		andView.addObject("pessoaobj", new Pessoa()); // objeto vazio

		return andView;

	}

	@GetMapping("/editarpessoa/{idpessoa}")
	public ModelAndView editar(@PathVariable("idpessoa") Long idpessoa) {

		Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa); // carrega o objeto do banco de dados
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa"); // serve pra retornar pra mesma
																					// tela
		modelAndView.addObject("pessoaobj", pessoa.get());
		modelAndView.addObject("profissoes", profissaoRepository.findAll());

		return modelAndView;

	}

	@GetMapping("/excluirpessoa/{idpessoa}")
	public ModelAndView excluir(@PathVariable("idpessoa") Long idpessoa) {

		pessoaRepository.deleteById(idpessoa);
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa"); // serve pra retornar pra mesma
																					// tela
		modelAndView.addObject("pessoa", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		modelAndView.addObject("pessoaobj", new Pessoa()); // objeto vazio
		return modelAndView;

	}

	@PostMapping("/pesquisarnome")
	public ModelAndView pesquisar(@RequestParam("nomepesquisa") String nomepesquisa,
			@RequestParam("sexopesquisa") String sexopesquisa) {

		List<Pessoa> pessoas = new ArrayList<Pessoa>();

		if (sexopesquisa != null && !sexopesquisa.isEmpty()) {
			pessoas = pessoaRepository.findPessoaByNomeSexo(nomepesquisa, sexopesquisa);
		} else {
			pessoas = pessoaRepository.findPessoaByNome(nomepesquisa);
		}

		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		modelAndView.addObject("pessoa", pessoas);
		modelAndView.addObject("pessoaobj", new Pessoa()); // objeto vazio

		return modelAndView;

	}

	@GetMapping("**/baixarcurriculo/{idpessoa}")
	public void baixarcurriculo(@PathVariable("idpessoa") Long idpessoa, HttpServletResponse response)
			throws IOException {

		// Consultar o objeto pessoa no banco de dados
		Pessoa pessoa = pessoaRepository.findById(idpessoa).get();
		if (pessoa.getCurriculo() != null) {

			// Setar tamanho da resposta
			response.setContentLength(pessoa.getCurriculo().length);

			// Tipo do arquivo para download ou pode ser generica application/octet-stream
			response.setContentType(pessoa.getTipoFileCurriculo());

			// Define o cabeçalho da resposta
			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; finename=\"%s\"", pessoa.getNomeFileCurriculo());
			response.setHeader(headerKey, headerValue);

			// finaliza a resposta
			response.getOutputStream().write(pessoa.getCurriculo());
		}
	}

//	@PostMapping("/pesquisarnome")
//	public ModelAndView pesquisar(@RequestParam("nomepesquisa")String nomepesquisa){
//		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
//		modelAndView.addObject("pessoa", pessoaRepository.findPessoaByNome(nomepesquisa));
//		modelAndView.addObject("pessoaobj", new Pessoa());
//		
//		return modelAndView;
//		}

//	@PostMapping("/pesquisarnome")
//	public ModelAndView pesquisar(@RequestParam("nomepesquisa") String nomepesquisa) {
//		 ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
//
//		if (nomepesquisa == null || nomepesquisa.isEmpty()) {
//			Iterable<Pessoa> pessoasIt = pessoaRepository.findAll();
//			modelAndView.addObject("pessoa", pessoasIt);
//			modelAndView.addObject("pessoaobj", new Pessoa());
//			return modelAndView;
//		}
//		return modelAndView;
//	}

	@GetMapping("/pesquisarnome")
	public void imprimePdf(@RequestParam("nomepesquisa") String nomepesquisa,
			@RequestParam("sexopesquisa") String sexopesquisa, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		List<Pessoa> pessoas = new ArrayList<Pessoa>();

		if (sexopesquisa != null && !sexopesquisa.isEmpty() && nomepesquisa != null && !nomepesquisa.isEmpty()) { // Busca// por// nome// e// sexo
				
			pessoas = pessoaRepository.findPessoaByNomeSexo(nomepesquisa, sexopesquisa);

		} else if (nomepesquisa != null && !nomepesquisa.isEmpty()) { // Busca somente por nome
			pessoas = pessoaRepository.findPessoaByNome(nomepesquisa);

		} else if (sexopesquisa != null && !sexopesquisa.isEmpty()) { // Busca somente por sexo
			pessoas = pessoaRepository.findPessoaBySexo(sexopesquisa);

		} else { // Busca todos
			Iterable<Pessoa> iterable = pessoaRepository.findAll();

			for (Pessoa pessoa : iterable) {
				pessoas.add(pessoa);
			}
		}

		// Chama o serviço que faz a geração do relatorio
		byte[] pdf = reportUtil.gerarRelatorio(pessoas, "pessoa", request.getServletContext());

		// Tamanho da resposta
		response.setContentLength(pdf.length);

		// Define na resposta o tipo do arquivo
		response.setContentType("application/octet-stream");

		// Define o cabeçalho da resposta
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", "relatorio.pdf");
		response.setHeader(headerKey, headerValue);

		// Finaliza a resposta pro navegador
		response.getOutputStream().write(pdf);
	}

	@GetMapping("/telefones/{idpessoa}")
	public ModelAndView telefones(@PathVariable("idpessoa") Long idpessoa) {

		Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa); // carrega o objeto do banco de dados
		ModelAndView modelAndView = new ModelAndView("cadastro/telefone"); // serve pra retornar pra mesma
		// tela
		modelAndView.addObject("pessoaobj", pessoa.get());
		modelAndView.addObject("telefone", telefoneRepository.getTelefone(idpessoa));

		return modelAndView;

	}

	@PostMapping("/salvartelefone/{pessoaid}")
	public ModelAndView salvartelefone(Telefone telefone, @PathVariable("pessoaid") Long pessoaid) {

		Pessoa pessoa = pessoaRepository.findById(pessoaid).get();

		if (telefone != null && telefone.getNumero().isEmpty() || telefone.getTipo().isEmpty()) {
			ModelAndView modelAndView = new ModelAndView("cadastro/telefone");
			modelAndView.addObject("pessoaobj", pessoa);
			modelAndView.addObject("telefone", telefoneRepository.getTelefone(pessoaid));
			List<String> msg = new ArrayList<String>();
			if (telefone.getNumero().isEmpty()) {

				msg.add("Campo Número Obrigatório");
			}

			if (telefone.getTipo().isEmpty()) {
				msg.add("Campo Tipo Obrigatório");
			}

			modelAndView.addObject("msg", msg);
			return modelAndView;

		}

		telefone.setPessoa(pessoa);
		telefoneRepository.save(telefone);

		ModelAndView modelAndView = new ModelAndView("cadastro/telefone");
		modelAndView.addObject("pessoaobj", pessoa);
		modelAndView.addObject("telefone", telefoneRepository.getTelefone(pessoaid));

		return modelAndView;
	}

	@GetMapping("/excluirtelefone/{idtelefone}")
	public ModelAndView excluirTelefone(@PathVariable("idtelefone") Long idtelefone) {

		Pessoa pessoa = telefoneRepository.findById(idtelefone).get().getPessoa();
		telefoneRepository.deleteById(idtelefone);
		ModelAndView modelAndView = new ModelAndView("cadastro/telefone"); // serve pra retornar pra mesma
																			// tela
		modelAndView.addObject("pessoaobj", pessoa);
		modelAndView.addObject("telefone", telefoneRepository.getTelefone(pessoa.getId()));

		return modelAndView;

	}
	
	@GetMapping("/pessoapag")
	public ModelAndView paginacaoPessoa(@PageableDefault(size = 5) Pageable pageable, ModelAndView model) {
		
		Page<Pessoa> pagePessoa = pessoaRepository.findAll(pageable);
		model.addObject("pessoa", pagePessoa);
		model.addObject("pessoaobj", new Pessoa());
		model.setViewName("cadastro/cadastropessoa");
		
		return model;
	}

}
